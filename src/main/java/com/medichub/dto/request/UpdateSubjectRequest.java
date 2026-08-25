package com.medichub.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** Rename / reorder / (de)activate a subject. */
public record UpdateSubjectRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @Min(0) Integer orderIndex,
        @NotNull Boolean active
) {
}
