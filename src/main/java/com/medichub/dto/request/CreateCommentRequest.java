package com.medichub.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A student's question on a course, optionally scoped to a topic. */
public record CreateCommentRequest(
        @NotBlank @Size(max = 5000) String text,
        Long topicId
) {
}
