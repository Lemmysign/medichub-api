package com.medichub.dto.response;

import java.time.Instant;
import java.util.List;

public record AttemptDetailResponse(
        Long id,
        Long testId,
        int scorePercent,
        boolean passed,
        Instant startedAt,
        Instant submittedAt,
        List<AttemptAnswerResponse> answers
) {
}
