package com.medichub.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Edit a Recall paper's tags/title (not its questions — those have their own endpoints). */
public record UpdateRecallRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        @NotNull Long subjectId,
        @NotNull @Min(1950) @Max(2100) Integer examYear
) {
}
