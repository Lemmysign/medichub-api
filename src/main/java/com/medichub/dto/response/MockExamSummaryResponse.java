package com.medichub.dto.response;

import com.medichub.model.enums.TestKind;

/**
 * Student list item for an available standalone exam (MCQ or Recall).
 * {@code bestScorePercent} is null if never attempted; {@code examYear} is set only for Recalls.
 */
public record MockExamSummaryResponse(
        Long id,
        String title,
        String description,
        int passMarkPercent,
        Integer durationMinutes,
        long questionCount,
        Integer bestScorePercent,
        long attemptCount,
        TestKind kind,
        Long subjectId,
        String subjectName,
        Integer examYear
) {
}
