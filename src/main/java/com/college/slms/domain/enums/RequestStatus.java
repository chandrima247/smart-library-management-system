package com.college.slms.domain.enums;

/**
 * Lifecycle of a student's borrow request.
 */
public enum RequestStatus {
    PENDING,
    APPROVED,
    REJECTED,
    CANCELLED,
    FULFILLED   // a loan has been issued against this request
}
