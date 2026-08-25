package com.medichub.exception;

/**
 * Thrown when a user with valid credentials tries to log in before confirming their email OTP.
 * The global handler maps this to 403 with error code {@code EMAIL_NOT_VERIFIED} so the SPA can
 * route the user to the verification screen.
 */
public class EmailNotVerifiedException extends RuntimeException {
    public EmailNotVerifiedException(String message) {
        super(message);
    }
}
