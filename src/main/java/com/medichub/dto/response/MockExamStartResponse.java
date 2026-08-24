package com.medichub.dto.response;

import com.medichub.model.enums.FeedbackMode;

import java.time.Instant;
import java.util.List;

/**
 * Returned when a student starts a mock attempt. For timed mocks the server anchors the
 * clock: {@code expiresAt} = startedAt + duration (+ any paused time). {@code expiresAt}
 * is null for untimed mocks. {@code feedbackMode} tells the client whether to reveal
 * per-question (IMMEDIATE) or only after submission.
 */
public record MockExamStartResponse(
        Long attemptId,
        Long mockExamId,
        String title,
        int passMarkPercent,
        Integer durationMinutes,
        FeedbackMode feedbackMode,
        Instant startedAt,
        Instant expiresAt,
        List<StudentQuestionResponse> questions
) {
}
