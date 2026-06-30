package com.college.slms.repository;

import com.college.slms.domain.Loan;
import com.college.slms.domain.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByStudentIdOrderByIssuedAtDesc(Long studentId);

    Page<Loan> findByStudentIdOrderByIssuedAtDesc(Long studentId, Pageable pageable);

    List<Loan> findByStudentIdAndStatusIn(Long studentId, List<LoanStatus> statuses);

    long countByStudentIdAndStatusIn(Long studentId, List<LoanStatus> statuses);

    long countByStatus(LoanStatus status);

    Page<Loan> findByStatusInOrderByDueDateAsc(List<LoanStatus> statuses, Pageable pageable);

    /** Active HOME loans whose due date is strictly before the given date. */
    @Query("""
            SELECT l FROM Loan l
            WHERE l.status = com.college.slms.domain.enums.LoanStatus.ACTIVE
              AND l.dueDate IS NOT NULL
              AND l.dueDate < :date
            """)
    List<Loan> findOverdueAsOf(@Param("date") LocalDate date);

    /** Active HOME loans due exactly on the given date (for reminders). */
    @Query("""
            SELECT l FROM Loan l
            WHERE l.status = com.college.slms.domain.enums.LoanStatus.ACTIVE
              AND l.dueDate = :date
            """)
    List<Loan> findDueOn(@Param("date") LocalDate date);
}
