package com.medichub.dto.response;

import java.util.List;

/** The test as presented to a student to attempt — never exposes correct answers. */
public record StudentTestResponse(
        Long id,
        Long courseId,
        String title,
        int passMarkPercent,
        List<StudentQuestionResponse> questions
) {
}
