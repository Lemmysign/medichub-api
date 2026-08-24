package com.medichub.dto.response;

import com.medichub.model.enums.FeedbackMode;

public record TestResponse(
        Long id,
        Long courseId,
        String title,
        int passMarkPercent,
        FeedbackMode feedbackMode,
        long questionCount
) {
}
