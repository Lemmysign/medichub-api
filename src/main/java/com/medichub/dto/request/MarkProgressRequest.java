package com.medichub.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Client reports total seconds watched for a topic; the server decides completion. */
public record MarkProgressRequest(
        @NotNull @PositiveOrZero Integer secondsWatched
) {
}
