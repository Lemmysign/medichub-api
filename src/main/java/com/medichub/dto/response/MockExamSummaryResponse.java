package com.medichub.dto.response;

/** Student list item for available mock exams. {@code bestScorePercent} is null if never attempted. */
public record MockExamSummaryResponse(
        Long id,
        String title,
        String description,
        int passMarkPercent,
        Integer durationMinutes,
        long questionCount,
        Integer bestScorePercent,
        long attemptCount
) {
}
