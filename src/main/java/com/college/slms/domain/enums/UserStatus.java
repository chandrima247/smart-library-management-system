package com.college.slms.domain.enums;

/**
 * Account lifecycle. New self-registrations start as {@link #PENDING} and must be
 * approved by an administrator before they can authenticate.
 */
public enum UserStatus {
    PENDING,
    ACTIVE,
    SUSPENDED,
    REJECTED
}
