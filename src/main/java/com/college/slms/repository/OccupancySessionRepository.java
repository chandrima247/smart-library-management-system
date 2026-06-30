package com.college.slms.repository;

import com.college.slms.domain.OccupancySession;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OccupancySessionRepository extends JpaRepository<OccupancySession, Long> {

    /** The student's currently open session, if they are checked in. */
    Optional<OccupancySession> findFirstByStudentIdAndCheckOutAtIsNull(Long studentId);

    /** All readers currently inside the library (open sessions). */
    List<OccupancySession> findByCheckOutAtIsNullOrderByCheckInAtDesc();

    /** Live occupancy count. */
    long countByCheckOutAtIsNull();
}
