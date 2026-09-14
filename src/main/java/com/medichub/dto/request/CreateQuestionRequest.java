package com.medichub.dto.request;

import com.medichub.model.enums.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

/** {@code type} may be null → defaults to MULTIPLE_CHOICE. At least two options, one correct.
 *  {@code imageUrl} is optional (a Cloudinary URL) for image-based questions. */
public record CreateQuestionRequest(
        @NotBlank @Size(max = 2000) String text,
        QuestionType type,
        @Size(max = 4000) String explanation,
        @Size(max = 1000) String imageUrl,
        @NotEmpty @Size(min = 2, max = 10) @Valid List<CreateOptionRequest> options
) {
}
