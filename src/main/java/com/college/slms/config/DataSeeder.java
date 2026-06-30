package com.college.slms.config;

import com.college.slms.domain.Book;
import com.college.slms.domain.BookCopy;
import com.college.slms.domain.User;
import com.college.slms.domain.enums.Role;
import com.college.slms.domain.enums.UserStatus;
import com.college.slms.repository.BookRepository;
import com.college.slms.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

/**
 * Seeds a realistic starter dataset (accounts + catalogue) on first launch so the
 * system is immediately demonstrable. Idempotent: it does nothing if users exist.
 * Excluded from the test profile to keep tests deterministic.
 */
@Component
@Profile("!test")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      BookRepository bookRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("Seeding initial SLMS dataset ...");
        seedUsers();
        seedCatalogue();
        log.info("Seed complete. Default logins — admin/admin123, librarian/librarian123, student/student123");
    }

    private void seedUsers() {
        userRepository.save(active("admin", "admin123", "System Administrator",
                "admin@college.edu", Role.ADMIN, "ADM-2026-0001", "IT Services"));
        userRepository.save(active("librarian", "librarian123", "Maria Quinn",
                "librarian@college.edu", Role.LIBRARIAN, "LIB-2026-0001", "Central Library"));
        userRepository.save(active("student", "student123", "Alex Mercer",
                "alex@college.edu", Role.STUDENT, "ST-2026-0001", "Computer Science"));
        userRepository.save(active("jordan", "student123", "Jordan Lee",
                "jordan@college.edu", Role.STUDENT, "ST-2026-0002", "Design"));

        // A couple of accounts awaiting approval to demonstrate the admin workflow.
        userRepository.save(pending("samira", "student123", "Samira Khan",
                "samira@college.edu", Role.STUDENT, "ST-2026-0003", "Psychology"));
        userRepository.save(pending("dpatel", "librarian123", "Dev Patel",
                "dev@college.edu", Role.LIBRARIAN, "LIB-2026-0002", "Reference Desk"));
    }

    private void seedCatalogue() {
        bookRepository.saveAll(List.of(
                book("9780132350884", "Clean Code", "Robert C. Martin", "Prentice Hall",
                        "Software Engineering", "A-12", 3,
                        "A handbook of agile software craftsmanship."),
                book("9780201633610", "Design Patterns", "Erich Gamma et al.", "Addison-Wesley",
                        "Software Engineering", "A-14", 2,
                        "Elements of reusable object-oriented software."),
                book("9780262033848", "Introduction to Algorithms", "Cormen, Leiserson, Rivest, Stein", "MIT Press",
                        "Computer Science", "B-03", 4,
                        "The classic comprehensive text on algorithms."),
                book("9780596007126", "Head First Design Patterns", "Eric Freeman", "O'Reilly",
                        "Software Engineering", "A-15", 2,
                        "A brain-friendly guide to design patterns."),
                book("9780134685991", "Effective Java", "Joshua Bloch", "Addison-Wesley",
                        "Programming", "A-09", 3,
                        "Best practices for the Java platform."),
                book("9781449331818", "Learning JavaScript Design Patterns", "Addy Osmani", "O'Reilly",
                        "Programming", "A-21", 2,
                        "Classical and modern patterns in JavaScript."),
                book("9780262035613", "Deep Learning", "Ian Goodfellow", "MIT Press",
                        "Artificial Intelligence", "C-01", 2,
                        "A comprehensive introduction to deep learning."),
                book("9780321125217", "Domain-Driven Design", "Eric Evans", "Addison-Wesley",
                        "Software Engineering", "A-30", 1,
                        "Tackling complexity in the heart of software.")
        ));
    }

    private User active(String username, String pwd, String name, String email,
                        Role role, String code, String dept) {
        User u = base(username, pwd, name, email, role, code, dept);
        u.setStatus(UserStatus.ACTIVE);
        u.approve(0L);
        return u;
    }

    private User pending(String username, String pwd, String name, String email,
                         Role role, String code, String dept) {
        User u = base(username, pwd, name, email, role, code, dept);
        u.setStatus(UserStatus.PENDING);
        return u;
    }

    private User base(String username, String pwd, String name, String email,
                      Role role, String code, String dept) {
        User u = new User(username, passwordEncoder.encode(pwd), name, email, role);
        u.setMemberCode(code);
        u.setDepartment(dept);
        return u;
    }

    private Book book(String isbn, String title, String author, String publisher,
                      String category, String shelf, int copies, String description) {
        Book b = new Book(isbn, title, author);
        b.setPublisher(publisher);
        b.setCategory(category);
        b.setLanguage("en");
        b.setShelfLocation(shelf);
        b.setPublishedDate(String.valueOf(Year.now().getValue() - 3));
        b.setDescription(description);
        for (int i = 1; i <= copies; i++) {
            String tail = isbn.substring(isbn.length() - 6);
            BookCopy copy = new BookCopy(b, "ACC-%s-%02d".formatted(tail, i));
            b.addCopy(copy);
        }
        return b;
    }
}
