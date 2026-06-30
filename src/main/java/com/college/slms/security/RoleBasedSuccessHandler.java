package com.college.slms.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Routes a freshly authenticated user to the dashboard appropriate for their
 * role, so a single login form serves all three portals.
 */
@Component
public class RoleBasedSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        String target = "/";
        if (roles.contains("ROLE_ADMIN")) {
            target = "/admin/dashboard";
        } else if (roles.contains("ROLE_LIBRARIAN")) {
            target = "/librarian/dashboard";
        } else if (roles.contains("ROLE_STUDENT")) {
            target = "/student/dashboard";
        }
        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
