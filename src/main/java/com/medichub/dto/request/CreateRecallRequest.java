package com.medichub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Create a Recall paper (past questions) — the instructor bulk-upload entry point.
 * Recalls are view-only study material, not exams, so there is no pass mark, duration, or
 * feedback mode: just the taxonomy tags ({@code subjectId} + {@code examYear}) and the questions.
 * {@code questions} may be supplied inline (parsed from an uploaded spreadsheet client-side) to
 * load the whole paper at once, or left empty to add them afterwards.
 */
public record CreateRecallRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @NotNull Long subjectId,
        @NotNull @Min(1950) @Max(2100) Integer examYear,
        @Size(max = 500) @Valid List<CreateQuestionRequest> questions
) {
}
