package com.medichub.dto.request;

import jakarta.validation.constraints.NotNull;

/** One answer: which option the student selected for a question ({@code selectedOptionId} null = skipped). */
public record AnswerSubmission(
        @NotNull Long questionId,
        Long selectedOptionId
) {
}
