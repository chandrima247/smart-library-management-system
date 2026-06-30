package com.college.slms.web;

import com.college.slms.config.SlmsProperties;
import com.college.slms.domain.enums.Role;
import com.college.slms.domain.enums.UserStatus;
import com.college.slms.service.CurrentUserService;
import com.college.slms.service.DashboardService;
import com.college.slms.service.UserService;
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

/**
 * Administrator console: registration approvals, user management and a read-only
 * view of the system configuration.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final CurrentUserService currentUser;
    private final DashboardService dashboardService;
    private final UserService userService;
    private final SlmsProperties properties;

    public AdminController(CurrentUserService currentUser,
                           DashboardService dashboardService,
                           UserService userService,
                           SlmsProperties properties) {
        this.currentUser = currentUser;
        this.dashboardService = dashboardService;
        this.userService = userService;
        this.properties = properties;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.adminStats());
        model.addAttribute("pendingUsers",
                userService.findByStatus(UserStatus.PENDING, PageRequest.of(0, 6)).getContent());
        return "admin/dashboard";
    }

    @GetMapping("/approvals")
    public String approvals(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<?> pending = userService.findByStatus(UserStatus.PENDING,
                PageRequest.of(page, 15, Sort.by("createdAt")));
        model.addAttribute("users", pending.getContent());
        model.addAttribute("pageInfo", pending);
        return "admin/approvals";
    }

    @PostMapping("/approvals/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes ra) {
        userService.approve(id, currentUser.currentId());
        ra.addFlashAttribute("successMessage", "Account approved.");
        return "redirect:/admin/approvals";
    }

    @PostMapping("/approvals/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes ra) {
        userService.reject(id, currentUser.currentId());
        ra.addFlashAttribute("successMessage", "Registration rejected.");
        return "redirect:/admin/approvals";
    }

    @GetMapping("/users")
    public String users(@RequestParam(defaultValue = "STUDENT") Role role,
                        @RequestParam(defaultValue = "0") int page,
                        Model model) {
        Page<?> users = userService.findByRole(role, PageRequest.of(page, 15, Sort.by("fullName")));
        model.addAttribute("users", users.getContent());
        model.addAttribute("pageInfo", users);
        model.addAttribute("role", role);
        model.addAttribute("roles", Role.values());
        return "admin/users";
    }

    @PostMapping("/users/{id}/status")
    public String setStatus(@PathVariable Long id, @RequestParam UserStatus status, RedirectAttributes ra) {
        userService.setStatus(id, status);
        ra.addFlashAttribute("successMessage", "User status updated to " + status + ".");
        return "redirect:/admin/users";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        model.addAttribute("circulation", properties.getCirculation());
        model.addAttribute("library", properties.getLibrary());
        model.addAttribute("googleBooks", properties.getGoogleBooks());
        return "admin/settings";
    }
}
