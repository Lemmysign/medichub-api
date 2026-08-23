package com.medichub.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Returned when a student starts a timed mock attempt. The server anchors the clock:
 * {@code expiresAt} = startedAt + duration, and the submit is validated against it.
 */
public record MockExamStartResponse(
        Long attemptId,
        Long mockExamId,
        String title,
        int passMarkPercent,
        Integer durationMinutes,
        Instant startedAt,
        Instant expiresAt,
        List<StudentQuestionResponse> questions
) {
}
