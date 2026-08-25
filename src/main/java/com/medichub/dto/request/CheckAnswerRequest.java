package com.medichub.dto.request;

import java.util.ArrayList;
import java.util.List;

/**
 * Immediate-mode per-question check. {@code selectedOptionIds} holds every option the student
 * picked (multiple for multiple-choice); {@code selectedOptionId} is a legacy single-value fallback.
 */
public record CheckAnswerRequest(
        Long selectedOptionId,
        List<Long> selectedOptionIds
) {
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
