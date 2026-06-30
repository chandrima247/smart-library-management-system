package com.college.slms.domain.enums;

/**
 * Lifecycle of an issued loan. {@link #OVERDUE} is derived for HOME loans whose
 * due date has passed and is persisted by the reminder scheduler for fast queries.
 */
public enum LoanStatus {
    ACTIVE,
    RETURNED,
    OVERDUE
}
