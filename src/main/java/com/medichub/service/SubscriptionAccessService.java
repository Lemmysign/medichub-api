package com.medichub.service;

/**
 * Central subscription access gate (CLAUDE.md §6). Content endpoints call this;
 * it is the single source of truth for "may this student consume content?".
 */
public interface SubscriptionAccessService {

    /** True if the student has an ACTIVE subscription whose endDate is in the future. */
    boolean hasActiveAccess(Long studentId);

    /** Throws {@code SubscriptionRequiredException} (402) if access is not active. */
    void requireActiveAccess(Long studentId);
}
