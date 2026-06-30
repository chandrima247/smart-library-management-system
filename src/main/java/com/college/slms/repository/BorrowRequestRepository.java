package com.college.slms.repository;

import com.college.slms.domain.BorrowRequest;
import com.college.slms.domain.enums.RequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BorrowRequestRepository extends JpaRepository<BorrowRequest, Long> {

    Page<BorrowRequest> findByStatusOrderByCreatedAtAsc(RequestStatus status, Pageable pageable);

    List<BorrowRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);

    long countByStatus(RequestStatus status);

    long countByStudentIdAndStatus(Long studentId, RequestStatus status);

    boolean existsByStudentIdAndBookIdAndStatus(Long studentId, Long bookId, RequestStatus status);
}
