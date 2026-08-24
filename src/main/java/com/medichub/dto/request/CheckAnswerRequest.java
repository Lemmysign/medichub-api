package com.medichub.dto.request;

/** Immediate-mode per-question check: the option the student picked ({@code null} = skipped). */
public record CheckAnswerRequest(
        Long selectedOptionId
) {
}
