package com.medichub.dto.response;

public record CourseProgressResponse(
        Long courseId,
        long totalTopics,
        long completedTopics,
        int percentComplete
) {
}
