package com.medichub.exception;

/**
 * Thrown when an authenticated user attempts an action they are not permitted to
 * perform (e.g. an instructor editing another instructor's course). Maps to HTTP 403.
 * Distinct from Spring Security's own AccessDeniedException, which is raised at the
 * filter layer for URL-level denials.
 */
public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
