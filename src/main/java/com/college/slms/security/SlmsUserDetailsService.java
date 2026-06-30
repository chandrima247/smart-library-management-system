package com.college.slms.security;

import com.college.slms.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Loads users for authentication. Accepts either the username or the email
 * address as the login identifier for convenience.
 */
@Service
public class SlmsUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public SlmsUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        return userRepository.findByUsernameIgnoreCase(identifier)
                .or(() -> userRepository.findByEmailIgnoreCase(identifier))
                .map(SlmsUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("No account for '" + identifier + "'"));
    }
}
