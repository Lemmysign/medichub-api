package com.medichub.dto.response;

import com.medichub.model.enums.QuestionType;

import java.util.List;

public record StudentQuestionResponse(
        Long id,
        String text,
        QuestionType type,
        int orderIndex,
        String imageUrl,
        List<StudentOptionResponse> options
) {
}
