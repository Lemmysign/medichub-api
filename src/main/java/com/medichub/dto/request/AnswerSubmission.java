package com.medichub.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * One submitted answer. {@code selectedOptionIds} holds every option the student picked
 * (multiple for multiple-choice; one for single-choice/true-false; empty/null = skipped).
 * {@code selectedOptionId} is a legacy single-value fallback for older clients.
 */
public record AnswerSubmission(
        @NotNull Long questionId,
        Long selectedOptionId,
        List<Long> selectedOptionIds
) {
    /** Normalised set of picks, merging the legacy single field. */
    public List<Long> effectiveSelectedIds() {
        if (selectedOptionIds != null && !selectedOptionIds.isEmpty()) {
            return selectedOptionIds;
        }
        List<Long> out = new ArrayList<>();
        if (selectedOptionId != null) {
            out.add(selectedOptionId);
        }
        return out;
    }
}
