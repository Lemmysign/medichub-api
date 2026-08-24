package com.medichub.dto.response;

/** Per-question result in an attempt review. {@code correctOptionId}/{@code explanation}
 *  are populated so the student can see the right answer after the attempt. */
public record AttemptAnswerResponse(
        Long questionId,
        String questionText,
        Long selectedOptionId,
        Long correctOptionId,
        String explanation,
        boolean correct
) {
}
