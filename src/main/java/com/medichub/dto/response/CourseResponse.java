package com.medichub.dto.response;

import java.time.Instant;

/** Summary view of a course for lists and management screens. */
public record CourseResponse(
        Long id,
        String title,
        String description,
        String thumbnailUrl,
        boolean published,
        Long instructorId,
        String instructorName,
        long topicCount,
        Instant createdAt,
        Instant updatedAt
) {
}
