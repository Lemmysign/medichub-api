package com.medichub.dto.request;

import com.medichub.model.enums.FeedbackMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code feedbackMode} may be null → defaults to IMMEDIATE for course tests. */
public record CreateTestRequest(
        @NotBlank @Size(max = 200) String title,
        @NotNull @Min(0) @Max(100) Integer passMarkPercent,
        FeedbackMode feedbackMode
) {
}
