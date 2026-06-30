package com.college.slms.web;

import com.college.slms.domain.Book;
import com.college.slms.domain.User;
import com.college.slms.domain.enums.LoanStatus;
import com.college.slms.domain.enums.RequestStatus;
import com.college.slms.service.BookService;
import com.college.slms.service.CirculationService;
import com.college.slms.service.CurrentUserService;
import com.college.slms.service.DashboardService;
import com.college.slms.service.OccupancyService;
import com.college.slms.service.dto.BookMetadata;
import com.college.slms.repository.BorrowRequestRepository;
import com.college.slms.repository.LoanRepository;
import com.college.slms.web.form.BookForm;
import com.college.slms.web.view.BookView;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Librarian portal: review borrow requests, issue and return loans, manage the
 * inventory (ISBN-assisted), and monitor current readers.
 */
@Controller
@RequestMapping("/librarian")
public class LibrarianController {

    private static final List<LoanStatus> ACTIVE = List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE);

    private final CurrentUserService currentUser;
    private final DashboardService dashboardService;
    private final CirculationService circulationService;
    private final BookService bookService;
    private final OccupancyService occupancyService;
    private final BorrowRequestRepository requestRepository;
    private final LoanRepository loanRepository;

    public LibrarianController(CurrentUserService currentUser,
                               DashboardService dashboardService,
                               CirculationService circulationService,
                               BookService bookService,
                               OccupancyService occupancyService,
                               BorrowRequestRepository requestRepository,
                               LoanRepository loanRepository) {
        this.currentUser = currentUser;
        this.dashboardService = dashboardService;
        this.circulationService = circulationService;
        this.bookService = bookService;
        this.occupancyService = occupancyService;
        this.requestRepository = requestRepository;
        this.loanRepository = loanRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.librarianStats());
        model.addAttribute("pendingRequests",
                requestRepository.findByStatusOrderByCreatedAtAsc(RequestStatus.PENDING, PageRequest.of(0, 6)).getContent());
        model.addAttribute("overdueLoans",
                loanRepository.findByStatusInOrderByDueDateAsc(List.of(LoanStatus.OVERDUE), PageRequest.of(0, 6)).getContent());
        return "librarian/dashboard";
    }

    // ----------------------------------------------------------- requests

    @GetMapping("/requests")
    public String requests(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<?> pending = requestRepository.findByStatusOrderByCreatedAtAsc(RequestStatus.PENDING, PageRequest.of(page, 15));
        model.addAttribute("requests", pending.getContent());
        model.addAttribute("pageInfo", pending);
        return "librarian/requests";
    }

    @PostMapping("/requests/{id}/approve")
    public String approve(@PathVariable Long id, @RequestParam(required = false) String note, RedirectAttributes ra) {
        circulationService.approveRequest(id, currentUser.require(), note);
        ra.addFlashAttribute("successMessage", "Request approved and a copy reserved.");
        return "redirect:/librarian/requests";
    }

    @PostMapping("/requests/{id}/reject")
    public String reject(@PathVariable Long id, @RequestParam(required = false) String note, RedirectAttributes ra) {
        circulationService.rejectRequest(id, currentUser.require(), note);
        ra.addFlashAttribute("successMessage", "Request rejected.");
        return "redirect:/librarian/requests";
    }

    @PostMapping("/requests/{id}/issue")
    public String issue(@PathVariable Long id, RedirectAttributes ra) {
        circulationService.issueAgainstRequest(id, currentUser.require());
        ra.addFlashAttribute("successMessage", "Loan issued to the student.");
        return "redirect:/librarian/circulation";
    }

    // -------------------------------------------------------- circulation

    @GetMapping("/circulation")
    public String circulation(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<?> active = loanRepository.findByStatusInOrderByDueDateAsc(ACTIVE, PageRequest.of(page, 15));
        model.addAttribute("loans", active.getContent());
        model.addAttribute("pageInfo", active);
        model.addAttribute("approvedRequests",
                requestRepository.findByStatusOrderByCreatedAtAsc(RequestStatus.APPROVED, PageRequest.of(0, 20)).getContent());
        return "librarian/circulation";
    }

    @PostMapping("/loans/{id}/return")
    public String returnLoan(@PathVariable Long id, RedirectAttributes ra) {
        circulationService.returnLoan(id, currentUser.require());
        ra.addFlashAttribute("successMessage", "Book returned and copy made available.");
        return "redirect:/librarian/circulation";
    }

    // ----------------------------------------------------------- inventory

    @GetMapping("/inventory")
    public String inventory(@RequestParam(required = false) String q,
                            @RequestParam(defaultValue = "0") int page,
                            Model model) {
        Page<Book> results = bookService.search(q, null, PageRequest.of(page, 12, Sort.by("title")));
        model.addAttribute("books", results.getContent().stream()
                .map(b -> new BookView(b, bookService.availableCopies(b.getId()))).toList());
        model.addAttribute("pageInfo", results);
        model.addAttribute("q", q);
        if (!model.containsAttribute("bookForm")) {
            model.addAttribute("bookForm", new BookForm());
        }
        return "librarian/inventory";
    }

    /** AJAX endpoint: resolve metadata for an ISBN to prefill the add-book form. */
    @GetMapping("/inventory/isbn")
    @ResponseBody
    public BookMetadata lookupIsbn(@RequestParam String isbn) {
        return bookService.previewIsbn(isbn);
    }

    @PostMapping("/inventory")
    public String addBook(@Valid @ModelAttribute("bookForm") BookForm form,
                          BindingResult binding,
                          RedirectAttributes ra) {
        if (binding.hasErrors()) {
            ra.addFlashAttribute("org.springframework.validation.BindingResult.bookForm", binding);
            ra.addFlashAttribute("bookForm", form);
            ra.addFlashAttribute("errorMessage", "Please correct the highlighted fields.");
            return "redirect:/librarian/inventory";
        }
        Book book = bookService.createBook(form);
        ra.addFlashAttribute("successMessage",
                "Added \"" + book.getTitle() + "\" with " + form.getCopies() + " cop/copies.");
        return "redirect:/librarian/inventory";
    }

    @PostMapping("/inventory/{bookId}/copies")
    public String addCopies(@PathVariable Long bookId, @RequestParam int count, RedirectAttributes ra) {
        int added = bookService.addCopies(bookId, count);
        ra.addFlashAttribute("successMessage", "Added " + added + " copy/copies.");
        return "redirect:/librarian/inventory";
    }

    // -------------------------------------------------------------- readers

    @GetMapping("/readers")
    public String readers(Model model) {
        model.addAttribute("readers", occupancyService.currentReaders());
        model.addAttribute("occupancy", occupancyService.currentOccupancy());
        model.addAttribute("capacity", occupancyService.capacity());
        model.addAttribute("availableSeats", occupancyService.availableSeats());
        model.addAttribute("hallName", occupancyService.hallName());
        return "librarian/readers";
    }
}
