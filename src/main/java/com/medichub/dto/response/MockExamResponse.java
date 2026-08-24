package com.medichub.dto.response;

import com.medichub.model.enums.FeedbackMode;

/** Creator (instructor/admin) view of a mock exam. */
public record MockExamResponse(
        Long id,
        String title,
        String description,
        int passMarkPercent,
        Integer durationMinutes,
        boolean published,
        FeedbackMode feedbackMode,
        String ownerName,
        long questionCount
) {
}
