package com.college.slms.web.form;

import com.college.slms.domain.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Self-registration payload. Administrators are never self-registered; the form
 * is restricted to STUDENT and LIBRARIAN at the controller layer.
 */
public class RegistrationForm {

    @NotBlank
    @Size(min = 3, max = 64)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Use letters, numbers, dot, dash or underscore only")
    private String username;

    @NotBlank
    @Size(min = 2, max = 120)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 72, message = "Password must be 8-72 characters")
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotNull
    private Role role = Role.STUDENT;

    @Size(max = 120)
    private String department;

    @Size(max = 32)
    private String phone;

    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    // --- getters / setters ---

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
