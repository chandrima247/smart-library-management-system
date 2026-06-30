package com.college.slms.repository;

import com.college.slms.domain.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    /**
     * Full-text-ish catalogue search across title, author, ISBN and category.
     * Case-insensitive partial match; paginated for performance.
     */
    @Query("""
            SELECT b FROM Book b
            WHERE (:q IS NULL OR :q = ''
                   OR LOWER(b.title)    LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(b.author)   LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(b.isbn)     LIKE LOWER(CONCAT('%', :q, '%'))
                   OR LOWER(b.category) LIKE LOWER(CONCAT('%', :q, '%')))
              AND (:category IS NULL OR :category = '' OR b.category = :category)
            """)
    Page<Book> search(@Param("q") String query, @Param("category") String category, Pageable pageable);

    @Query("SELECT DISTINCT b.category FROM Book b WHERE b.category IS NOT NULL ORDER BY b.category")
    List<String> findDistinctCategories();
}
