package com.college.slms.repository;

import com.college.slms.domain.User;
import com.college.slms.domain.enums.Role;
import com.college.slms.domain.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByStatus(UserStatus status, Pageable pageable);

    Page<User> findByRole(Role role, Pageable pageable);

    long countByStatus(UserStatus status);

    long countByRole(Role role);
}
