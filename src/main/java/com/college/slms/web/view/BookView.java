package com.college.slms.web.view;

import com.college.slms.domain.Book;

/**
 * A catalogue row paired with its live available-copy count, computed in the
 * controller to avoid lazy-loading copies in the view (open-in-view is off).
 *
 * <p>The component is named {@code availableCount} (not {@code available}) so it
 * doesn't collide with a boolean {@code isAvailable()} accessor during SpEL
 * property resolution in templates.</p>
 */
public record BookView(Book book, long availableCount) {
    public boolean hasCopies() {
        return availableCount > 0;
    }
}
