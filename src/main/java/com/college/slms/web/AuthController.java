package com.college.slms.web;

import com.college.slms.domain.User;
import com.college.slms.domain.enums.Role;
import com.college.slms.service.UserService;
import com.college.slms.web.form.RegistrationForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Public authentication surface: the unified login portal and self-registration.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistrationForm());
        }
        model.addAttribute("roles", new Role[]{Role.STUDENT, Role.LIBRARIAN});
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("form") RegistrationForm form,
                           BindingResult binding,
                           RedirectAttributes redirectAttributes,
                           Model model) {
        if (!form.passwordsMatch()) {
            binding.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
        }
        if (binding.hasErrors()) {
            model.addAttribute("roles", new Role[]{Role.STUDENT, Role.LIBRARIAN});
            return "auth/register";
        }
        User created = userService.register(form);
        redirectAttributes.addFlashAttribute("successMessage",
                "Account created for " + created.getMemberCode()
                        + ". An administrator must approve it before you can sign in.");
        return "redirect:/login";
    }
}
