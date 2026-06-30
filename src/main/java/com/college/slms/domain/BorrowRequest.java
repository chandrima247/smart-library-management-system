package com.college.slms.domain;

import com.college.slms.domain.enums.BorrowType;
import com.college.slms.domain.enums.RequestStatus;
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

/**
 * A student's request to borrow a title, either to take {@link BorrowType#HOME}
 * or to {@link BorrowType#READING read in the library}. A librarian reviews the
 * request and, on approval, issues a {@link Loan} against a concrete copy.
 */
@Entity
@Table(name = "borrow_requests", indexes = {
        @Index(name = "ix_req_student_status", columnList = "student_id,status"),
        @Index(name = "ix_req_status", columnList = "status")
})
public class BorrowRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Enumerated(EnumType.STRING)
    @Column(name = "borrow_type", nullable = false, length = 16)
    private BorrowType borrowType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "decided_by")
    private User decidedBy;

    @Column(name = "decided_at")
    private Instant decidedAt;

    /** Librarian's note when rejecting (or any decision remark). */
    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    protected BorrowRequest() {
        // for JPA
    }

    public BorrowRequest(User student, Book book, BorrowType borrowType) {
        this.student = student;
        this.book = book;
        this.borrowType = borrowType;
    }

    public void approve(User librarian, String note) {
        this.status = RequestStatus.APPROVED;
        this.decidedBy = librarian;
        this.decidedAt = Instant.now();
        this.decisionNote = note;
    }

    public void reject(User librarian, String note) {
        this.status = RequestStatus.REJECTED;
        this.decidedBy = librarian;
        this.decidedAt = Instant.now();
        this.decisionNote = note;
    }

    public void markFulfilled() {
        this.status = RequestStatus.FULFILLED;
    }

    public void cancel() {
        this.status = RequestStatus.CANCELLED;
    }

    public boolean isPending() {
        return status == RequestStatus.PENDING;
    }

    // --- getters / setters ---

    public User getStudent() {
        return student;
    }

    public Book getBook() {
        return book;
    }

    public BorrowType getBorrowType() {
        return borrowType;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public User getDecidedBy() {
        return decidedBy;
    }

    public Instant getDecidedAt() {
        return decidedAt;
    }

    public String getDecisionNote() {
        return decisionNote;
    }
}
