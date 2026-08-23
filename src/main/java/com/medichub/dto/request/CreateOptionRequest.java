package com.medichub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOptionRequest(
        @NotBlank @Size(max = 1000) String text,
        @NotNull Boolean correct
) {
}
