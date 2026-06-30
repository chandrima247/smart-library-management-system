package com.college.slms.service;

import com.college.slms.domain.User;
import com.college.slms.exception.ResourceNotFoundException;
import com.college.slms.repository.UserRepository;
import com.college.slms.security.SlmsUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolves the {@link User} entity for the currently authenticated principal.
 * Keeps the {@code SecurityContext} lookup in one place.
 */
@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public User require() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SlmsUserDetails principal)) {
            throw new ResourceNotFoundException("No authenticated user in context");
        }
        return userRepository.findById(principal.getId())
                .orElseThrow(() -> ResourceNotFoundException.of("User", principal.getId()));
    }

    public Long currentId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SlmsUserDetails principal) {
            return principal.getId();
        }
        return null;
    }
}
