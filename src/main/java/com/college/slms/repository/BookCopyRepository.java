package com.college.slms.repository;

import com.college.slms.domain.BookCopy;
import com.college.slms.domain.enums.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookCopyRepository extends JpaRepository<BookCopy, Long> {

    Optional<BookCopy> findByAccessionNumber(String accessionNumber);

    Optional<BookCopy> findByBarcode(String barcode);

    boolean existsByAccessionNumber(String accessionNumber);

    List<BookCopy> findByBookId(Long bookId);

    long countByBookIdAndStatus(Long bookId, CopyStatus status);

    long countByStatus(CopyStatus status);

    /**
     * Locks and returns the first available copy of a book for issuing. Used inside
     * a transaction to avoid two librarians issuing the same physical copy.
     */
    @Query("""
            SELECT c FROM BookCopy c
            WHERE c.book.id = :bookId AND c.status = com.college.slms.domain.enums.CopyStatus.AVAILABLE
            ORDER BY c.id ASC
            """)
    List<BookCopy> findAvailableCopies(@Param("bookId") Long bookId);
}
