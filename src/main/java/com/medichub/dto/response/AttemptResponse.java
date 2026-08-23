package com.medichub.dto.response;

import java.time.Instant;

public record AttemptResponse(
        Long id,
        Long testId,
        int scorePercent,
        boolean passed,
        Instant startedAt,
        Instant submittedAt
) {
}
