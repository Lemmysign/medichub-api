package com.medichub.exception;

/**
 * Raised when a payment-provider (Paystack) call cannot be completed — e.g. the
 * provider is not configured yet, or returned an error. Maps to HTTP 503 so the
 * client shows a clean "payments unavailable" message rather than a raw 500.
 */
public class PaymentException extends RuntimeException {

    public PaymentException(String message) {
        super(message);
    }
}
