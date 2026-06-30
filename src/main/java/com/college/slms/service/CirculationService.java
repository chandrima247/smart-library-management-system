package com.college.slms.service;

import com.college.slms.config.SlmsProperties;
import com.college.slms.domain.Book;
import com.college.slms.domain.BookCopy;
import com.college.slms.domain.BorrowRequest;
import com.college.slms.domain.Fine;
import com.college.slms.domain.Loan;
import com.college.slms.domain.User;
import com.college.slms.domain.enums.BorrowType;
import com.college.slms.domain.enums.CopyStatus;
import com.college.slms.domain.enums.FineStatus;
import com.college.slms.domain.enums.LoanStatus;
import com.college.slms.domain.enums.NotificationType;
import com.college.slms.domain.enums.RequestStatus;
import com.college.slms.exception.BusinessRuleException;
import com.college.slms.exception.ResourceNotFoundException;
import com.college.slms.repository.BookCopyRepository;
import com.college.slms.repository.BookRepository;
import com.college.slms.repository.BorrowRequestRepository;
import com.college.slms.repository.FineRepository;
import com.college.slms.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * The circulation engine. Coordinates borrow requests, librarian decisions, loan
 * issuing for both HOME and in-library READING, returns and overdue fines, while
 * keeping copy availability and notifications consistent. All mutating operations
 * are transactional and rely on optimistic locking to stay correct under
 * concurrent librarian actions.
 */
@Service
public class CirculationService {

    private final BorrowRequestRepository requestRepository;
    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final FineRepository fineRepository;
    private final NotificationService notificationService;
    private final SlmsProperties.Circulation rules;

    public CirculationService(BorrowRequestRepository requestRepository,
                              LoanRepository loanRepository,
                              BookRepository bookRepository,
                              BookCopyRepository copyRepository,
                              FineRepository fineRepository,
                              NotificationService notificationService,
                              SlmsProperties properties) {
        this.requestRepository = requestRepository;
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.fineRepository = fineRepository;
        this.notificationService = notificationService;
        this.rules = properties.getCirculation();
    }

    // ----------------------------------------------------------------- requests

    /** A student requests to borrow a title for HOME or READING. */
    @Transactional
    public BorrowRequest createRequest(User student, Long bookId, BorrowType type) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> ResourceNotFoundException.of("Book", bookId));

        if (requestRepository.existsByStudentIdAndBookIdAndStatus(student.getId(), bookId, RequestStatus.PENDING)) {
            throw new BusinessRuleException("You already have a pending request for this title.");
        }
        if (type == BorrowType.HOME) {
            BigDecimal pendingFines = fineRepository.sumPendingByStudent(student.getId());
            if (pendingFines.compareTo(BigDecimal.ZERO) > 0) {
                throw new BusinessRuleException(
                        "Settle your outstanding fine of $" + pendingFines + " before borrowing for home.");
            }
            long active = loanRepository.countByStudentIdAndStatusIn(student.getId(),
                    List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE));
            if (active >= rules.getMaxActiveLoans()) {
                throw new BusinessRuleException(
                        "You have reached the borrowing limit of " + rules.getMaxActiveLoans() + " books.");
            }
        }
        return requestRepository.save(new BorrowRequest(student, book, type));
    }

    @Transactional
    public void cancelRequest(Long requestId, Long studentId) {
        BorrowRequest request = getRequest(requestId);
        if (!request.getStudent().getId().equals(studentId)) {
            throw new BusinessRuleException("You can only cancel your own requests.");
        }
        if (!request.isPending()) {
            throw new BusinessRuleException("Only pending requests can be cancelled.");
        }
        request.cancel();
    }

    /** Librarian approves a pending request, reserving an available copy. */
    @Transactional
    public BorrowRequest approveRequest(Long requestId, User librarian, String note) {
        BorrowRequest request = getRequest(requestId);
        if (!request.isPending()) {
            throw new BusinessRuleException("This request has already been decided.");
        }
        BookCopy copy = firstAvailableCopy(request.getBook().getId());
        copy.setStatus(CopyStatus.RESERVED);

        request.approve(librarian, note);
        notificationService.notify(request.getStudent(), NotificationType.REQUEST_APPROVED,
                "Request approved",
                "Your %s request for \"%s\" was approved. Collect it from the circulation desk."
                        .formatted(request.getBorrowType().name().toLowerCase(), request.getBook().getTitle()),
                "/student/history");
        return request;
    }

    @Transactional
    public BorrowRequest rejectRequest(Long requestId, User librarian, String note) {
        BorrowRequest request = getRequest(requestId);
        if (!request.isPending()) {
            throw new BusinessRuleException("This request has already been decided.");
        }
        request.reject(librarian, note);
        notificationService.notify(request.getStudent(), NotificationType.REQUEST_REJECTED,
                "Request rejected",
                "Your request for \"%s\" was rejected.%s".formatted(
                        request.getBook().getTitle(),
                        note != null && !note.isBlank() ? " Reason: " + note : ""),
                "/student/history");
        return request;
    }

    // -------------------------------------------------------------------- loans

    /**
     * Issue a loan against an approved request. Picks the reserved/available copy,
     * sets the appropriate copy status and (for HOME) a due date.
     */
    @Transactional
    public Loan issueAgainstRequest(Long requestId, User librarian) {
        BorrowRequest request = getRequest(requestId);
        if (request.getStatus() != RequestStatus.APPROVED) {
            throw new BusinessRuleException("Only approved requests can be issued.");
        }
        BookCopy copy = reservedOrAvailableCopy(request.getBook().getId());
        Loan loan = buildLoan(request.getStudent(), copy, librarian, request.getBorrowType());
        loan.setRequest(request);
        request.markFulfilled();
        loanRepository.save(loan);
        notifyIssued(loan);
        return loan;
    }

    /** Direct walk-in issue without a prior request (e.g. at the desk). */
    @Transactional
    public Loan issueDirect(User student, Long copyId, User librarian, BorrowType type) {
        BookCopy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Copy", copyId));
        if (!copy.isAvailable()) {
            throw new BusinessRuleException("That copy is not available for issue.");
        }
        if (type == BorrowType.HOME && copy.isReferenceOnly()) {
            throw new BusinessRuleException("Reference-only copies cannot be taken home.");
        }
        Loan loan = buildLoan(student, copy, librarian, type);
        loanRepository.save(loan);
        notifyIssued(loan);
        return loan;
    }

    /** Return an issued loan; raises an overdue fine where applicable. */
    @Transactional
    public Loan returnLoan(Long loanId, User librarian) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> ResourceNotFoundException.of("Loan", loanId));
        if (!loan.isActive()) {
            throw new BusinessRuleException("This loan has already been returned.");
        }
        loan.markReturned(librarian);
        loan.getCopy().setStatus(CopyStatus.AVAILABLE);

        Fine fine = raiseOverdueFineIfNeeded(loan);
        notificationService.notify(loan.getStudent(), NotificationType.RETURN,
                "Book returned",
                "\"%s\" has been returned. Thank you!".formatted(loan.getCopy().getBook().getTitle()),
                "/student/history");
        if (fine != null) {
            notificationService.notify(loan.getStudent(), NotificationType.FINE,
                    "Overdue fine applied",
                    "A fine of $%s was applied for the late return of \"%s\"."
                            .formatted(fine.getAmount(), loan.getCopy().getBook().getTitle()),
                    "/student/history");
        }
        return loan;
    }

    // ----------------------------------------------------------------- helpers

    private Loan buildLoan(User student, BookCopy copy, User librarian, BorrowType type) {
        LocalDate due = (type == BorrowType.HOME)
                ? LocalDate.now().plusDays(rules.getHomeLoanDays())
                : null;
        copy.setStatus(type == BorrowType.HOME ? CopyStatus.ON_LOAN : CopyStatus.IN_READING);
        return new Loan(student, copy, librarian, type, due);
    }

    private void notifyIssued(Loan loan) {
        String due = loan.getDueDate() != null ? " Due back on " + loan.getDueDate() + "." : " (in-library reading)";
        notificationService.notify(loan.getStudent(), NotificationType.ISSUE,
                "Book issued",
                "\"%s\" has been issued to you.%s".formatted(loan.getCopy().getBook().getTitle(), due),
                "/student/history");
    }

    private Fine raiseOverdueFineIfNeeded(Loan loan) {
        if (loan.getBorrowType() != BorrowType.HOME || loan.getDueDate() == null) {
            return null;
        }
        long daysLate = ChronoUnit.DAYS.between(loan.getDueDate(), LocalDate.now());
        if (daysLate <= 0) {
            return null;
        }
        BigDecimal amount = rules.getFinePerDay().multiply(BigDecimal.valueOf(daysLate));
        Fine fine = new Fine(loan.getStudent(), loan, amount,
                "Overdue by %d day(s)".formatted(daysLate));
        return fineRepository.save(fine);
    }

    private BookCopy firstAvailableCopy(Long bookId) {
        List<BookCopy> available = copyRepository.findAvailableCopies(bookId);
        if (available.isEmpty()) {
            throw new BusinessRuleException("No copies are currently available for this title.");
        }
        return available.get(0);
    }

    private BookCopy reservedOrAvailableCopy(Long bookId) {
        return copyRepository.findByBookId(bookId).stream()
                .filter(c -> c.getStatus() == CopyStatus.RESERVED)
                .findFirst()
                .orElseGet(() -> firstAvailableCopy(bookId));
    }

    private BorrowRequest getRequest(Long id) {
        return requestRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Request", id));
    }

    // --------------------------------------------------------------- settlement

    @Transactional
    public void settleFine(Long fineId, FineStatus status, User staff) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> ResourceNotFoundException.of("Fine", fineId));
        fine.settle(status, staff);
    }
}
