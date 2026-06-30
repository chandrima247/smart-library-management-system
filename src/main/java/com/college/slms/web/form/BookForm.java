package com.college.slms.web.form;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Create/edit payload for a catalogue title. The ISBN may be used to prefill the
 * remaining fields via Google Books before submission.
 */
public class BookForm {

    @NotBlank
    @Size(max = 20)
    private String isbn;

    @NotBlank
    @Size(max = 300)
    private String title;

    @Size(max = 300)
    private String author;

    @Size(max = 160)
    private String publisher;

    @Size(max = 40)
    private String publishedDate;

    @Size(max = 80)
    private String category;

    @Size(max = 16)
    private String language;

    @Size(max = 60)
    private String shelfLocation;

    @Size(max = 512)
    private String coverUrl;

    @Size(max = 4000)
    private String description;

    /** Number of physical copies to create when adding a new title. */
    @Min(1)
    @Max(500)
    private int copies = 1;

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

    public String getShelfLocation() {
        return shelfLocation;
    }

    public void setShelfLocation(String shelfLocation) {
        this.shelfLocation = shelfLocation;
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

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }
}
