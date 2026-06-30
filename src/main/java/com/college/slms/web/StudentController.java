package com.college.slms.web;

import com.college.slms.domain.Book;
import com.college.slms.domain.User;
import com.college.slms.domain.enums.BorrowType;
import com.college.slms.domain.enums.LoanStatus;
import com.college.slms.service.BookService;
import com.college.slms.service.CirculationService;
import com.college.slms.service.CurrentUserService;
import com.college.slms.service.DashboardService;
import com.college.slms.service.OccupancyService;
import com.college.slms.web.view.BookView;
import com.college.slms.repository.BorrowRequestRepository;
import com.college.slms.repository.FineRepository;
import com.college.slms.repository.LoanRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Student portal: dashboard, catalogue search & borrow requests, personal
 * history, and reading-hall check-in/out.
 */
@Controller
@RequestMapping("/student")
public class StudentController {

    private final CurrentUserService currentUser;
    private final DashboardService dashboardService;
    private final BookService bookService;
    private final CirculationService circulationService;
    private final OccupancyService occupancyService;
    private final LoanRepository loanRepository;
    private final BorrowRequestRepository requestRepository;
    private final FineRepository fineRepository;

    public StudentController(CurrentUserService currentUser,
                            DashboardService dashboardService,
                            BookService bookService,
                            CirculationService circulationService,
                            OccupancyService occupancyService,
                            LoanRepository loanRepository,
                            BorrowRequestRepository requestRepository,
                            FineRepository fineRepository) {
        this.currentUser = currentUser;
        this.dashboardService = dashboardService;
        this.bookService = bookService;
        this.circulationService = circulationService;
        this.occupancyService = occupancyService;
        this.loanRepository = loanRepository;
        this.requestRepository = requestRepository;
        this.fineRepository = fineRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        User me = currentUser.require();
        model.addAttribute("stats", dashboardService.studentStats(me.getId()));
        model.addAttribute("activeLoans",
                loanRepository.findByStudentIdAndStatusIn(me.getId(), List.of(LoanStatus.ACTIVE, LoanStatus.OVERDUE)));
        // A light "recommended" strip: the most recently added available titles.
        Page<Book> recent = bookService.search(null, null,
                PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "createdAt")));
        model.addAttribute("recommended", toViews(recent.getContent()));
        model.addAttribute("occupancy", occupancyService.currentOccupancy());
        model.addAttribute("capacity", occupancyService.capacity());
        model.addAttribute("availableSeats", occupancyService.availableSeats());
        model.addAttribute("hallName", occupancyService.hallName());
        model.addAttribute("checkedIn", occupancyService.isCheckedIn(me.getId()));
        return "student/dashboard";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q,
                         @RequestParam(required = false) String category,
                         @RequestParam(defaultValue = "0") int page,
                         Model model) {
        Page<Book> results = bookService.search(q, category, PageRequest.of(page, 12, Sort.by("title")));
        model.addAttribute("books", toViews(results.getContent()));
        model.addAttribute("pageInfo", results);
        model.addAttribute("q", q);
        model.addAttribute("category", category);
        model.addAttribute("categories", bookService.categories());
        return "student/search";
    }

    @PostMapping("/request")
    public String requestBorrow(@RequestParam Long bookId,
                                @RequestParam BorrowType borrowType,
                                RedirectAttributes ra) {
        User me = currentUser.require();
        circulationService.createRequest(me, bookId, borrowType);
        ra.addFlashAttribute("successMessage",
                "Request submitted. You'll be notified once a librarian reviews it.");
        return "redirect:/student/search";
    }

    @PostMapping("/request/{id}/cancel")
    public String cancelRequest(@PathVariable Long id, RedirectAttributes ra) {
        circulationService.cancelRequest(id, currentUser.currentId());
        ra.addFlashAttribute("successMessage", "Request cancelled.");
        return "redirect:/student/history";
    }

    @GetMapping("/history")
    public String history(Model model) {
        User me = currentUser.require();
        model.addAttribute("requests", requestRepository.findByStudentIdOrderByCreatedAtDesc(me.getId()));
        model.addAttribute("loans", loanRepository.findByStudentIdOrderByIssuedAtDesc(me.getId()));
        model.addAttribute("fines", fineRepository.findByStudentIdOrderByCreatedAtDesc(me.getId()));
        return "student/history";
    }

    @GetMapping("/library")
    public String library(Model model) {
        User me = currentUser.require();
        model.addAttribute("occupancy", occupancyService.currentOccupancy());
        model.addAttribute("capacity", occupancyService.capacity());
        model.addAttribute("availableSeats", occupancyService.availableSeats());
        model.addAttribute("hallName", occupancyService.hallName());
        model.addAttribute("checkedIn", occupancyService.isCheckedIn(me.getId()));
        return "student/library";
    }

    @PostMapping("/checkin")
    public String checkIn(RedirectAttributes ra) {
        occupancyService.checkIn(currentUser.require());
        ra.addFlashAttribute("successMessage", "Checked in. Enjoy your study session!");
        return "redirect:/student/library";
    }

    @PostMapping("/checkout")
    public String checkOut(RedirectAttributes ra) {
        occupancyService.checkOut(currentUser.require());
        ra.addFlashAttribute("successMessage", "Checked out. See you next time!");
        return "redirect:/student/library";
    }

    private List<BookView> toViews(List<Book> books) {
        return books.stream()
                .map(b -> new BookView(b, bookService.availableCopies(b.getId())))
                .toList();
    }
}
