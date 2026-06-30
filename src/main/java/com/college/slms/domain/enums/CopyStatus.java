package com.college.slms.domain.enums;

/**
 * Physical state of a single {@code BookCopy} (identified by accession number).
 */
public enum CopyStatus {
    AVAILABLE,
    ON_LOAN,      // checked out for HOME borrow
    IN_READING,   // issued for in-library READING
    RESERVED,     // held against an approved request awaiting pickup
    LOST,
    DAMAGED,
    WITHDRAWN
}
