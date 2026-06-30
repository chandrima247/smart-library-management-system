package com.college.slms.domain;

import com.college.slms.domain.enums.BorrowType;
import com.college.slms.domain.enums.LoanStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * An issued circulation transaction binding a {@link BookCopy} to a student.
 * Covers both HOME borrows (with a due date) and in-library READING sessions
 * (returned the same day). Returned loans are retained for full history and
 * auditability rather than deleted.
 */
@Entity
@Table(name = "loans", indexes = {
        @Index(name = "ix_loan_student_status", columnList = "student_id,status"),
        @Index(name = "ix_loan_copy", columnList = "copy_id"),
        @Index(name = "ix_loan_status_due", columnList = "status,due_date")
})
public class Loan extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "copy_id", nullable = false)
    private BookCopy copy;

    /** The request this loan fulfils (nullable for walk-in issues). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id")
    private BorrowRequest request;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "issued_by", nullable = false)
    private User issuedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "borrow_type", nullable = false, length = 16)
    private BorrowType borrowType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private LoanStatus status = LoanStatus.ACTIVE;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt = Instant.now();

    /** Due date for HOME loans; null for in-library READING sessions. */
    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "returned_at")
    private Instant returnedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "returned_to")
    private User returnedTo;

    protected Loan() {
        // for JPA
    }

    public Loan(User student, BookCopy copy, User issuedBy, BorrowType borrowType, LocalDate dueDate) {
        this.student = student;
        this.copy = copy;
        this.issuedBy = issuedBy;
        this.borrowType = borrowType;
        this.dueDate = dueDate;
    }

    public boolean isActive() {
        return status == LoanStatus.ACTIVE || status == LoanStatus.OVERDUE;
    }

    public boolean isOverdue(LocalDate today) {
        return isActive() && dueDate != null && today.isAfter(dueDate);
    }

    public void markReturned(User librarian) {
        this.status = LoanStatus.RETURNED;
        this.returnedAt = Instant.now();
        this.returnedTo = librarian;
    }

    // --- getters / setters ---

    public User getStudent() {
        return student;
    }

    public BookCopy getCopy() {
        return copy;
    }

    public BorrowRequest getRequest() {
        return request;
    }

    public void setRequest(BorrowRequest request) {
        this.request = request;
    }

    public User getIssuedBy() {
        return issuedBy;
    }

    public BorrowType getBorrowType() {
        return borrowType;
    }

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    /** Convenience for views: issue timestamp as a local date. */
    public LocalDate getIssuedDate() {
        return issuedAt == null ? null : LocalDate.ofInstant(issuedAt, ZoneId.systemDefault());
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Instant getReturnedAt() {
        return returnedAt;
    }

    public User getReturnedTo() {
        return returnedTo;
    }
}
