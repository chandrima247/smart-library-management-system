package com.college.slms.web;

import com.college.slms.security.SlmsUserDetails;
import com.college.slms.service.NotificationService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Injects view-wide attributes (currently the unread-notification badge count)
 * into every controller response so the top bar can render consistently.
 */
@ControllerAdvice(basePackages = "com.college.slms.web")
public class GlobalModelAttributes {

    private final NotificationService notificationService;

    public GlobalModelAttributes(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @ModelAttribute("unreadCount")
    public Long unreadCount(@AuthenticationPrincipal SlmsUserDetails principal) {
        if (principal == null) {
            return 0L;
        }
        return notificationService.unreadCount(principal.getId());
    }
}
