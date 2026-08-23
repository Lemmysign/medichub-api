package com.medichub.dto.request;

import com.medichub.model.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Replaces the question text/type and its full option set. */
public record UpdateQuestionRequest(
        @NotBlank @Size(max = 2000) String text,
        QuestionType type,
        @NotEmpty @Size(min = 2, max = 10) @Valid List<CreateOptionRequest> options
) {
}
