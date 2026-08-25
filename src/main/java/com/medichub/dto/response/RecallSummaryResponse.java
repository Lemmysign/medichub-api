package com.medichub.dto.response;

/** Student list-card view of a Recall paper — click through to see its questions. */
public record RecallSummaryResponse(
        Long id,
        String title,
        String description,
        String subjectName,
        Integer examYear,
        long questionCount
) {
}
