package com.college.slms.service;

import com.college.slms.domain.User;
import com.college.slms.domain.enums.NotificationType;
import com.college.slms.domain.enums.Role;
import com.college.slms.domain.enums.UserStatus;
import com.college.slms.exception.BusinessRuleException;
import com.college.slms.exception.ResourceNotFoundException;
import com.college.slms.repository.UserRepository;
import com.college.slms.web.form.RegistrationForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;

/**
 * User lifecycle: self-registration (pending approval), administrator approval
 * and rejection, and account creation by staff.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       NotificationService notificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    /**
     * Register a new account in PENDING state. Administrators cannot be created
     * via public registration.
     */
    @Transactional
    public User register(RegistrationForm form) {
        if (form.getRole() == Role.ADMIN) {
            throw new BusinessRuleException("Administrator accounts cannot be self-registered.");
        }
        if (!form.passwordsMatch()) {
            throw new BusinessRuleException("Passwords do not match.");
        }
        if (userRepository.existsByUsernameIgnoreCase(form.getUsername())) {
            throw new BusinessRuleException("That username is already taken.");
        }
        if (userRepository.existsByEmailIgnoreCase(form.getEmail())) {
            throw new BusinessRuleException("An account with that email already exists.");
        }

        User user = new User(
                form.getUsername().trim(),
                passwordEncoder.encode(form.getPassword()),
                form.getFullName().trim(),
                form.getEmail().trim().toLowerCase(),
                form.getRole());
        user.setDepartment(form.getDepartment());
        user.setPhone(form.getPhone());
        user.setStatus(UserStatus.PENDING);
        user.setMemberCode(generateMemberCode(form.getRole()));
        return userRepository.save(user);
    }

    @Transactional
    public User approve(Long userId, Long approverId) {
        User user = getById(userId);
        if (user.getStatus() == UserStatus.ACTIVE) {
            return user;
        }
        user.approve(approverId);
        notificationService.notify(user, NotificationType.GENERAL,
                "Account approved",
                "Welcome to SLMS. Your account has been approved — you can now sign in and use the library.",
                "/login");
        return user;
    }

    @Transactional
    public User reject(Long userId, Long approverId) {
        User user = getById(userId);
        user.reject(approverId);
        return user;
    }

    @Transactional
    public void setStatus(Long userId, UserStatus status) {
        getById(userId).setStatus(status);
    }

    @Transactional(readOnly = true)
    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("User", id));
    }

    @Transactional(readOnly = true)
    public Page<User> findByStatus(UserStatus status, Pageable pageable) {
        return userRepository.findByStatus(status, pageable);
    }

    @Transactional(readOnly = true)
    public Page<User> findByRole(Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable);
    }

    @Transactional(readOnly = true)
    public long countByStatus(UserStatus status) {
        return userRepository.countByStatus(status);
    }

    @Transactional(readOnly = true)
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    /** Generates a readable member code such as ST-2024-0007 / LIB-2024-0003. */
    private String generateMemberCode(Role role) {
        String prefix = switch (role) {
            case ADMIN -> "ADM";
            case LIBRARIAN -> "LIB";
            case STUDENT -> "ST";
        };
        long sequence = userRepository.countByRole(role) + 1;
        return "%s-%d-%04d".formatted(prefix, Year.now().getValue(), sequence);
    }
}
