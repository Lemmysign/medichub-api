package com.medichub.exception;

/**
 * Thrown when a student without an ACTIVE, unexpired subscription tries to consume
 * gated content (video playback, materials). Maps to HTTP 402 Payment Required
 * (CLAUDE.md §6).
 */
public class SubscriptionRequiredException extends RuntimeException {

    public SubscriptionRequiredException(String message) {
        super(message);
    }
}
