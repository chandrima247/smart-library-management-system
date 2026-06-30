package com.college.slms.domain.enums;

/**
 * The two circulation modes the library supports. Both coexist.
 */
public enum BorrowType {
    /** Take the copy home for the configured loan period. */
    HOME,
    /** Read inside the library; the copy never leaves the premises. */
    READING
}
