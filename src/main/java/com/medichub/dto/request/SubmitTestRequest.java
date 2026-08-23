package com.medichub.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SubmitTestRequest(
        @NotNull @Valid List<AnswerSubmission> answers
) {
}
