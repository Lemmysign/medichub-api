package com.medichub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateMockExamRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @NotNull @Min(0) @Max(100) Integer passMarkPercent,
        @NotNull @Min(1) @Max(600) Integer durationMinutes
) {
}
