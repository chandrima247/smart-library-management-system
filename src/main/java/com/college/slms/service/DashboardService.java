package com.college.slms.service;

import com.college.slms.domain.enums.CopyStatus;
import com.college.slms.domain.enums.LoanStatus;
import com.college.slms.domain.enums.RequestStatus;
import com.college.slms.domain.enums.Role;
import com.college.slms.domain.enums.UserStatus;
import com.college.slms.repository.BookCopyRepository;
import com.college.slms.repository.BookRepository;
import com.college.slms.repository.BorrowRequestRepository;
import com.college.slms.repository.FineRepository;
import com.college.slms.repository.LoanRepository;
import com.college.slms.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Read-only aggregation of the headline figures shown on each role dashboard.
 * Centralising the counts keeps the controllers thin and the numbers consistent.
 */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    private static final List<LoanStatus> ACTIVE_STATES = List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final LoanRepository loanRepository;
    private final BorrowRequestRepository requestRepository;
    private final FineRepository fineRepository;
    private final OccupancyService occupancyService;

    public DashboardService(UserRepository userRepository,
                            BookRepository bookRepository,
                            BookCopyRepository copyRepository,
                            LoanRepository loanRepository,
                            BorrowRequestRepository requestRepository,
                            FineRepository fineRepository,
                            OccupancyService occupancyService) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.loanRepository = loanRepository;
        this.requestRepository = requestRepository;
        this.fineRepository = fineRepository;
        this.occupancyService = occupancyService;
    }

    public StudentStats studentStats(Long studentId) {
        return new StudentStats(
                loanRepository.countByStudentIdAndStatusIn(studentId, ACTIVE_STATES),
                requestRepository.countByStudentIdAndStatus(studentId, RequestStatus.PENDING),
                fineRepository.sumPendingByStudent(studentId),
                occupancyService.isCheckedIn(studentId));
    }

    public LibrarianStats librarianStats() {
        return new LibrarianStats(
                requestRepository.countByStatus(RequestStatus.PENDING),
                loanRepository.countByStatus(LoanStatus.ACTIVE),
                loanRepository.countByStatus(LoanStatus.OVERDUE),
                occupancyService.currentOccupancy(),
                occupancyService.availableSeats(),
                occupancyService.capacity());
    }

    public AdminStats adminStats() {
        return new AdminStats(
                userRepository.count(),
                userRepository.countByStatus(UserStatus.PENDING),
                userRepository.countByRole(Role.STUDENT),
                userRepository.countByRole(Role.LIBRARIAN),
                bookRepository.count(),
                copyRepository.count(),
                copyRepository.countByStatus(CopyStatus.AVAILABLE),
                loanRepository.countByStatus(LoanStatus.ACTIVE) + loanRepository.countByStatus(LoanStatus.OVERDUE),
                loanRepository.countByStatus(LoanStatus.OVERDUE),
                fineRepository.sumAllPending(),
                occupancyService.currentOccupancy(),
                occupancyService.capacity());
    }

    public record StudentStats(long activeLoans, long pendingRequests, BigDecimal pendingFines, boolean checkedIn) {
    }

    public record LibrarianStats(long pendingRequests, long activeLoans, long overdueLoans,
                                 long currentOccupancy, long availableSeats, int capacity) {
    }

    public record AdminStats(long totalUsers, long pendingApprovals, long students, long librarians,
                             long totalBooks, long totalCopies, long availableCopies,
                             long activeLoans, long overdueLoans, BigDecimal pendingFines,
                             long currentOccupancy, int capacity) {
    }
}
