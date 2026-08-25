package com.medichub.exception;

/**
 * Thrown when an instructor with a verified email tries to log in before an admin has approved
 * their account. The global handler maps this to 403 with error code {@code INSTRUCTOR_PENDING}
 * so the SPA can show the "awaiting approval" screen.
 */
public class InstructorNotApprovedException extends RuntimeException {
    public InstructorNotApprovedException(String message) {
        super(message);
    }
}
