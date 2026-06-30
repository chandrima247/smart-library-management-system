package com.college.slms.domain;

import com.college.slms.domain.enums.FineStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A monetary charge raised against a student, typically for an overdue or lost
 * item. Linked to the originating {@link Loan} for traceability.
 */
@Entity
@Table(name = "fines", indexes = {
        @Index(name = "ix_fine_student_status", columnList = "student_id,status")
})
public class Fine extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private FineStatus status = FineStatus.PENDING;

    @Column(length = 255)
    private String reason;

    @Column(name = "settled_at")
    private Instant settledAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "settled_by")
    private User settledBy;

    protected Fine() {
        // for JPA
    }

    public Fine(User student, Loan loan, BigDecimal amount, String reason) {
        this.student = student;
        this.loan = loan;
        this.amount = amount;
        this.reason = reason;
    }

    public void settle(FineStatus newStatus, User staff) {
        this.status = newStatus;
        this.settledAt = Instant.now();
        this.settledBy = staff;
    }

    // --- getters / setters ---

    public User getStudent() {
        return student;
    }

    public Loan getLoan() {
        return loan;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public FineStatus getStatus() {
        return status;
    }

    public String getReason() {
        return reason;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public User getSettledBy() {
        return settledBy;
    }
}
