package com.college.slms.service;

import com.college.slms.domain.Book;
import com.college.slms.domain.BookCopy;
import com.college.slms.domain.enums.CopyStatus;
import com.college.slms.exception.BusinessRuleException;
import com.college.slms.exception.ResourceNotFoundException;
import com.college.slms.repository.BookCopyRepository;
import com.college.slms.repository.BookRepository;
import com.college.slms.service.dto.BookMetadata;
import com.college.slms.web.form.BookForm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Catalogue management: ISBN-assisted title creation, copy (accession) handling
 * and search. Enforces the Book (title) / BookCopy (physical item) separation.
 */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository copyRepository;
    private final GoogleBooksClient googleBooksClient;

    public BookService(BookRepository bookRepository,
                       BookCopyRepository copyRepository,
                       GoogleBooksClient googleBooksClient) {
        this.bookRepository = bookRepository;
        this.copyRepository = copyRepository;
        this.googleBooksClient = googleBooksClient;
    }

    /** Best-effort metadata preview used to prefill the add-book form. */
    public BookMetadata previewIsbn(String isbn) {
        return googleBooksClient.lookupByIsbn(isbn);
    }

    /**
     * Create a new catalogue title plus the requested number of physical copies.
     * If only the ISBN was supplied, missing fields are enriched from Google Books.
     */
    @Transactional
    public Book createBook(BookForm form) {
        String isbn = form.getIsbn().replaceAll("[^0-9Xx]", "");
        if (bookRepository.existsByIsbn(isbn)) {
            throw new BusinessRuleException("A book with ISBN " + isbn + " already exists in the catalogue.");
        }

        // Enrich blanks from the external provider without overriding librarian input.
        if (isBlank(form.getTitle()) || isBlank(form.getAuthor())) {
            BookMetadata meta = googleBooksClient.lookupByIsbn(isbn);
            if (meta.resolved()) {
                if (isBlank(form.getTitle())) form.setTitle(meta.title());
                if (isBlank(form.getAuthor())) form.setAuthor(meta.author());
                if (isBlank(form.getPublisher())) form.setPublisher(meta.publisher());
                if (isBlank(form.getPublishedDate())) form.setPublishedDate(meta.publishedDate());
                if (isBlank(form.getCategory())) form.setCategory(meta.category());
                if (isBlank(form.getLanguage())) form.setLanguage(meta.language());
                if (isBlank(form.getCoverUrl())) form.setCoverUrl(meta.coverUrl());
                if (isBlank(form.getDescription())) form.setDescription(meta.description());
            }
        }
        if (isBlank(form.getTitle())) {
            throw new BusinessRuleException("Title is required (Google Books could not resolve this ISBN).");
        }

        Book book = new Book(isbn, form.getTitle().trim(),
                form.getAuthor() != null ? form.getAuthor().trim() : "Unknown");
        applyMetadata(book, form);

        for (int i = 0; i < Math.max(1, form.getCopies()); i++) {
            book.addCopy(new BookCopy(book, nextAccessionNumber(isbn, i)));
        }
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(Long bookId, BookForm form) {
        Book book = getById(bookId);
        book.setTitle(form.getTitle().trim());
        book.setAuthor(form.getAuthor());
        applyMetadata(book, form);
        return book;
    }

    /** Adds extra copies to an existing title and returns how many were added. */
    @Transactional
    public int addCopies(Long bookId, int count) {
        if (count < 1) {
            throw new BusinessRuleException("Copy count must be at least 1.");
        }
        Book book = getById(bookId);
        for (int i = 0; i < count; i++) {
            book.addCopy(new BookCopy(book, nextAccessionNumber(book.getIsbn(), i)));
        }
        return count;
    }

    @Transactional
    public void updateCopyStatus(Long copyId, CopyStatus status) {
        BookCopy copy = copyRepository.findById(copyId)
                .orElseThrow(() -> ResourceNotFoundException.of("Copy", copyId));
        copy.setStatus(status);
    }

    @Transactional(readOnly = true)
    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Book", id));
    }

    @Transactional(readOnly = true)
    public Page<Book> search(String query, String category, Pageable pageable) {
        return bookRepository.search(emptyToNull(query), emptyToNull(category), pageable);
    }

    @Transactional(readOnly = true)
    public List<String> categories() {
        return bookRepository.findDistinctCategories();
    }

    @Transactional(readOnly = true)
    public long availableCopies(Long bookId) {
        return copyRepository.countByBookIdAndStatus(bookId, CopyStatus.AVAILABLE);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> copiesOf(Long bookId) {
        return copyRepository.findByBookId(bookId);
    }

    private void applyMetadata(Book book, BookForm form) {
        book.setPublisher(form.getPublisher());
        book.setPublishedDate(form.getPublishedDate());
        book.setCategory(form.getCategory());
        book.setLanguage(form.getLanguage());
        book.setShelfLocation(form.getShelfLocation());
        book.setCoverUrl(form.getCoverUrl());
        book.setDescription(truncate(form.getDescription(), 4000));
    }

    /**
     * Builds a unique accession number. Combines the ISBN tail with a global
     * counter, retrying on the rare collision so concurrent additions stay unique.
     */
    private String nextAccessionNumber(String isbn, int offset) {
        String tail = isbn.length() >= 6 ? isbn.substring(isbn.length() - 6) : isbn;
        AtomicLong seq = new AtomicLong(copyRepository.count() + 1 + offset);
        String candidate;
        do {
            candidate = "ACC-%s-%05d".formatted(tail, seq.getAndIncrement());
        } while (copyRepository.existsByAccessionNumber(candidate));
        return candidate;
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
}
