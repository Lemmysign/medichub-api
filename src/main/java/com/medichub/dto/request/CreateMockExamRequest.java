package com.medichub.dto.request;

import com.medichub.model.enums.FeedbackMode;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** {@code durationMinutes} null = untimed. {@code feedbackMode} null → ON_SUBMISSION. */
public record CreateMockExamRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @NotNull @Min(0) @Max(100) Integer passMarkPercent,
        @Min(1) @Max(600) Integer durationMinutes,
        FeedbackMode feedbackMode
) {
}
