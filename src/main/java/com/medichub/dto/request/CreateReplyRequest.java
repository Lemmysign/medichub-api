package com.medichub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** An instructor's reply to a student question. */
public record CreateReplyRequest(
        @NotBlank @Size(max = 5000) String text
) {
}
