package com.college.slms.repository;

import com.college.slms.domain.Fine;
import com.college.slms.domain.enums.FineStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface FineRepository extends JpaRepository<Fine, Long> {

    List<Fine> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    List<Fine> findByStudentIdAndStatus(Long studentId, FineStatus status);

    boolean existsByLoanIdAndStatus(Long loanId, FineStatus status);

    @Query("""
            SELECT COALESCE(SUM(f.amount), 0) FROM Fine f
            WHERE f.student.id = :studentId AND f.status = com.college.slms.domain.enums.FineStatus.PENDING
            """)
    BigDecimal sumPendingByStudent(@Param("studentId") Long studentId);

    @Query("SELECT COALESCE(SUM(f.amount), 0) FROM Fine f WHERE f.status = com.college.slms.domain.enums.FineStatus.PENDING")
    BigDecimal sumAllPending();
}
