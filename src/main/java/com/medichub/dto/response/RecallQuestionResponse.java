package com.medichub.dto.response;

import com.medichub.model.enums.QuestionType;

import java.util.List;

/**
 * A single Recall question for the student study view — <b>answers revealed</b>.
 * Recalls are read-only past questions (no taking/submitting): each option carries its
 * {@code correct} flag and the explanation is always included. Tagged with subject + year.
 */
public record RecallQuestionResponse(
        Long id,
        String subjectName,
        Integer examYear,
        String sourceTitle,
        String text,
        QuestionType type,
        String explanation,
        List<OptionResponse> options
) {
}
