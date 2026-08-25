package com.medichub.dto.response;

import java.util.List;

/**
 * Per-question result in an attempt review. {@code selectedOptionIds} is everything the student
 * picked; {@code correctOptionIds} is every correct option (so multiple-choice shows its full
 * answer key). The singular {@code selectedOptionId}/{@code correctOptionId} are the first of each,
 * kept for older clients. {@code explanation} is populated for every question.
 */
public record AttemptAnswerResponse(
        Long questionId,
        String questionText,
        Long selectedOptionId,
        List<Long> selectedOptionIds,
        Long correctOptionId,
        List<Long> correctOptionIds,
        String explanation,
        boolean correct
) {
}
