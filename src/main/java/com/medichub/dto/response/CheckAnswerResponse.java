package com.medichub.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Immediate-mode reveal for one answered question. {@code correctOptionIds} lists <b>every</b>
 * correct option (so multiple-choice questions reveal their full answer key); {@code correctOptionId}
 * is the first of these, kept for older clients. {@code explanation} is always returned so the client
 * can show it whether the pick was right or wrong. {@code expiresAt} reflects the (possibly
 * paused-extended) deadline for timed mocks; null for untimed.
 */
public record CheckAnswerResponse(
        Long questionId,
        boolean correct,
        Long correctOptionId,
        List<Long> correctOptionIds,
        String explanation,
        boolean timerPaused,
        Instant expiresAt
) {
}
