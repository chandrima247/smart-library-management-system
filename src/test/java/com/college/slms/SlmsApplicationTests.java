package com.college.slms;

import com.college.slms.domain.Book;
import com.college.slms.domain.User;
import com.college.slms.domain.enums.BorrowType;
import com.college.slms.domain.enums.LoanStatus;
import com.college.slms.domain.enums.Role;
import com.college.slms.domain.Loan;
import com.college.slms.repository.BookCopyRepository;
import com.college.slms.repository.BookRepository;
import com.college.slms.repository.UserRepository;
import com.college.slms.service.CirculationService;
import com.college.slms.web.form.RegistrationForm;
import com.college.slms.service.UserService;
import com.college.slms.domain.BookCopy;
import com.college.slms.domain.enums.CopyStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.college.slms.domain.enums.UserStatus;
import com.college.slms.exception.BusinessRuleException;

/**
 * Smoke + behaviour tests covering the core circulation rules. Runs against an
 * in-memory database under the {@code test} profile (DataSeeder disabled).
 */
@SpringBootTest
@ActiveProfiles("test")
class SlmsApplicationTests {

    @Autowired
    UserService userService;
    @Autowired
    CirculationService circulationService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BookRepository bookRepository;
    @Autowired
    BookCopyRepository copyRepository;

    @Test
    void contextLoads() {
        assertThat(userService).isNotNull();
    }

    @Test
    void registrationStartsPendingAndCannotBeAdmin() {
        RegistrationForm form = new RegistrationForm();
        form.setUsername("tester1");
        form.setFullName("Test User");
        form.setEmail("tester1@college.edu");
        form.setPassword("password123");
        form.setConfirmPassword("password123");
        form.setRole(Role.STUDENT);

        User created = userService.register(form);
        assertThat(created.getStatus()).isEqualTo(UserStatus.PENDING);

        RegistrationForm adminAttempt = new RegistrationForm();
        adminAttempt.setUsername("hacker");
        adminAttempt.setFullName("Bad Actor");
        adminAttempt.setEmail("bad@college.edu");
        adminAttempt.setPassword("password123");
        adminAttempt.setConfirmPassword("password123");
        adminAttempt.setRole(Role.ADMIN);
        assertThatThrownBy(() -> userService.register(adminAttempt))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void homeLoanIsIssuedWithDueDateAndReturnFreesCopy() {
        User student = activeStudent("circStudent", "circ@college.edu");
        User librarian = librarian();
        Book book = bookWithCopy("9990000000001", "Test Title", "ACC-T-01");

        var request = circulationService.createRequest(student, book.getId(), BorrowType.HOME);
        circulationService.approveRequest(request.getId(), librarian, "ok");
        Loan loan = circulationService.issueAgainstRequest(request.getId(), librarian);

        assertThat(loan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
        assertThat(loan.getDueDate()).isNotNull();
        assertThat(loan.getCopy().getStatus()).isEqualTo(CopyStatus.ON_LOAN);

        circulationService.returnLoan(loan.getId(), librarian);
        BookCopy refreshed = copyRepository.findById(loan.getCopy().getId()).orElseThrow();
        assertThat(refreshed.getStatus()).isEqualTo(CopyStatus.AVAILABLE);
    }

    // --- helpers ---

    private User activeStudent(String username, String email) {
        User u = new User(username, "{noop}x", "Student " + username, email, Role.STUDENT);
        u.approve(0L);
        return userRepository.save(u);
    }

    private User librarian() {
        User u = new User("lib" + System.nanoTime(), "{noop}x", "Librarian", "lib" + System.nanoTime() + "@college.edu", Role.LIBRARIAN);
        u.approve(0L);
        return userRepository.save(u);
    }

    private Book bookWithCopy(String isbn, String title, String accession) {
        Book b = new Book(isbn, title, "Author");
        b.addCopy(new BookCopy(b, accession));
        return bookRepository.save(b);
    }
}
