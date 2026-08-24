package com.medichub.dto.response;

import com.medichub.model.enums.FeedbackMode;

import java.util.List;

/** The test as presented to a student to attempt — never exposes correct answers. */
public record StudentTestResponse(
        Long id,
        Long courseId,
        String title,
        int passMarkPercent,
        FeedbackMode feedbackMode,
        List<StudentQuestionResponse> questions
) {
}
