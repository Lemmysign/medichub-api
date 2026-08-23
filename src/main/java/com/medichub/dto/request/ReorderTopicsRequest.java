package com.medichub.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** New ordering for a course's topics: the full list of topic ids in desired order. */
public record ReorderTopicsRequest(
        @NotEmpty List<Long> topicIds
) {
}
