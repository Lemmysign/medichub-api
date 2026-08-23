package com.medichub.dto.response;

import com.medichub.model.enums.QuestionType;

import java.util.List;

/** Instructor view of a question (with correct answers). */
public record QuestionResponse(
        Long id,
        String text,
        QuestionType type,
        int orderIndex,
        List<OptionResponse> options
) {
}
