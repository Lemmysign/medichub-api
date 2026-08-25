package com.medichub.dto.response;

import com.medichub.model.enums.FeedbackMode;
import com.medichub.model.enums.TestKind;

/**
 * Creator (instructor/admin) view of a standalone exam — an MCQ or a Recall.
 * {@code subjectId}/{@code subjectName} are set for both; {@code examYear} only for Recalls.
 */
public record MockExamResponse(
        Long id,
        String title,
        String description,
        int passMarkPercent,
        Integer durationMinutes,
        boolean published,
        FeedbackMode feedbackMode,
        String ownerName,
        long questionCount,
        TestKind kind,
        Long subjectId,
        String subjectName,
        Integer examYear
) {
}
