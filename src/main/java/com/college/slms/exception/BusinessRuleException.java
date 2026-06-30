package com.college.slms.exception;

/**
 * Thrown when an operation is rejected by a domain/business rule (e.g. borrowing
 * while a fine is outstanding, no copies available, duplicate registration).
 * Mapped to HTTP 409 and surfaced to the user as a friendly message.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
