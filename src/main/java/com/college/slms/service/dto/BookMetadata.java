package com.college.slms.service.dto;

/**
 * Normalised bibliographic metadata resolved from an external provider
 * (Google Books). All fields are optional; callers must tolerate nulls so the
 * catalogue keeps working when a lookup is incomplete or unavailable.
 */
public record BookMetadata(
        String isbn,
        String title,
        String author,
        String publisher,
        String publishedDate,
        String category,
        String language,
        Integer pageCount,
        String coverUrl,
        String description,
        boolean resolved
) {
    /** A placeholder result used when no external metadata could be fetched. */
    public static BookMetadata unresolved(String isbn) {
        return new BookMetadata(isbn, null, null, null, null, null, null, null, null, null, false);
    }
}
