package com.college.slms.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * A bibliographic title, identified by its ISBN. A {@code Book} is the catalogue
 * record; the physical, lendable items are {@link BookCopy} rows. This title/copy
 * separation is a fixed requirement and must be preserved.
 *
 * <p>Metadata fields (publisher, pages, cover, description, …) are populated from
 * the Google Books API when available and remain nullable so the catalogue still
 * works when an ISBN lookup fails.</p>
 */
@Entity
@Table(name = "books", indexes = {
        @Index(name = "ux_books_isbn", columnList = "isbn", unique = true),
        @Index(name = "ix_books_title", columnList = "title"),
        @Index(name = "ix_books_author", columnList = "author")
})
public class Book extends BaseEntity {

    /** ISBN-13 (or ISBN-10) — the canonical identifier for the title. */
    @Column(nullable = false, unique = true, length = 20)
    private String isbn;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 300)
    private String author;

    @Column(length = 160)
    private String publisher;

    @Column(name = "published_date", length = 40)
    private String publishedDate;

    @Column(length = 80)
    private String category;

    @Column(length = 16)
    private String language;

    @Column(name = "page_count")
    private Integer pageCount;

    @Column(name = "cover_url", length = 512)
    private String coverUrl;

    @Column(length = 4000)
    private String description;

    /** Shelf / call-number location within the stacks. */
    @Column(name = "shelf_location", length = 60)
    private String shelfLocation;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookCopy> copies = new ArrayList<>();

    protected Book() {
        // for JPA
    }

    public Book(String isbn, String title, String author) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
    }

    public void addCopy(BookCopy copy) {
        copies.add(copy);
        copy.setBook(this);
    }

    // --- getters / setters ---

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
    }

    public List<BookCopy> getCopies() {
        return copies;
    }
}
