package com.medichub.dto.response;

public record AttemptAnswerResponse(
        Long questionId,
        String questionText,
        Long selectedOptionId,
        boolean correct
) {
}
