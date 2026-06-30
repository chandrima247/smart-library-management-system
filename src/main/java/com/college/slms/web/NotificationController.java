package com.college.slms.web;

import com.college.slms.service.CurrentUserService;
import com.college.slms.service.NotificationService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * In-app notification inbox, shared by all authenticated roles.
 */
@Controller
public class NotificationController {

    private final NotificationService notificationService;
    private final CurrentUserService currentUser;

    public NotificationController(NotificationService notificationService, CurrentUserService currentUser) {
        this.notificationService = notificationService;
        this.currentUser = currentUser;
    }

    @GetMapping("/notifications")
    public String inbox(Model model) {
        Long uid = currentUser.currentId();
        model.addAttribute("notifications", notificationService.recentFor(uid));
        return "notifications";
    }

    @PostMapping("/notifications/read-all")
    public String markAllRead(RedirectAttributes ra) {
        notificationService.markAllRead(currentUser.currentId());
        ra.addFlashAttribute("successMessage", "All notifications marked as read.");
        return "redirect:/notifications";
    }
}
