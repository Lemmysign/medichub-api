package com.medichub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Bulk-import a batch of questions (parsed from an uploaded spreadsheet on the client). */
public record BulkQuestionsRequest(
        @NotEmpty @Size(max = 500) @Valid List<CreateQuestionRequest> questions
) {
}
