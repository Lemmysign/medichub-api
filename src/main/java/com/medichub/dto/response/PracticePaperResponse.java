package com.medichub.dto.response;

import java.util.List;

/**
 * An MCQ paper opened in <b>practice mode</b> — untimed, ungraded, no attempt recorded. Questions
 * carry the correct answer + explanation so the SPA can reveal them the moment the student clicks.
 */
public record PracticePaperResponse(
        Long id,
        String title,
        String subjectName,
        List<QuestionResponse> questions
) {
}
