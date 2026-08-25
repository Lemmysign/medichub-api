package com.medichub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create a taxonomy subject. The slug and order are derived server-side if not overridden later. */
public record CreateSubjectRequest(
        @NotBlank @Size(max = 120) String name
) {
}
