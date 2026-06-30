package com.college.slms.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Duration;
import java.time.Instant;

/**
 * Records a student's physical presence in the reading hall. A session is opened
 * on arrival check-in and closed on check-out. Open sessions (null
 * {@code checkOutAt}) represent the current readers and drive live occupancy.
 */
@Entity
@Table(name = "occupancy_sessions", indexes = {
        @Index(name = "ix_occ_student_open", columnList = "student_id,check_out_at"),
        @Index(name = "ix_occ_open", columnList = "check_out_at")
})
public class OccupancySession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(name = "check_in_at", nullable = false)
    private Instant checkInAt = Instant.now();

    /** Null while the student is still inside the library. */
    @Column(name = "check_out_at")
    private Instant checkOutAt;

    @Column(length = 80)
    private String area;

    protected OccupancySession() {
        // for JPA
    }

    public OccupancySession(User student, String area) {
        this.student = student;
        this.area = area;
    }

    public boolean isOpen() {
        return checkOutAt == null;
    }

    public void checkOut() {
        this.checkOutAt = Instant.now();
    }

    public Duration getDuration() {
        Instant end = checkOutAt != null ? checkOutAt : Instant.now();
        return Duration.between(checkInAt, end);
    }

    /** Minutes elapsed since check-in — convenient for views. */
    public long getMinutesElapsed() {
        return getDuration().toMinutes();
    }

    // --- getters ---

    public User getStudent() {
        return student;
    }

    public Instant getCheckInAt() {
        return checkInAt;
    }

    public Instant getCheckOutAt() {
        return checkOutAt;
    }

    public String getArea() {
        return area;
    }
}
