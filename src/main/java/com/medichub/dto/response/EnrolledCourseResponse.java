package com.medichub.dto.response;

import java.time.Instant;

/** A student's enrolled course with per-course progress (the "my courses" list). */
public record EnrolledCourseResponse(
        Long courseId,
        String title,
        String thumbnailUrl,
        String instructorName,
        long totalTopics,
        long completedTopics,
        int percentComplete,
        Instant enrolledAt,
        Instant lastAccessedAt
) {
}
