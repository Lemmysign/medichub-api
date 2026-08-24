package com.medichub.dto.response;

import java.time.Instant;

/**
 * Immediate-mode reveal for one answered question. {@code correctOptionId} and
 * {@code explanation} are returned so the client can show the correct answer when the
 * student is wrong. {@code expiresAt} reflects the (possibly paused-extended) deadline
 * for timed mocks; null for untimed.
 */
public record CheckAnswerResponse(
        Long questionId,
        boolean correct,
        Long correctOptionId,
        String explanation,
        boolean timerPaused,
        Instant expiresAt
) {
}
