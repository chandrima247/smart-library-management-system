package com.college.slms.web;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Landing routing: send authenticated users to their role dashboard, everyone
 * else to the login portal.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String index(HttpServletRequest request) {
        if (request.isUserInRole("ADMIN")) {
            return "redirect:/admin/dashboard";
        }
        if (request.isUserInRole("LIBRARIAN")) {
            return "redirect:/librarian/dashboard";
        }
        if (request.isUserInRole("STUDENT")) {
            return "redirect:/student/dashboard";
        }
        return "redirect:/login";
    }
}
