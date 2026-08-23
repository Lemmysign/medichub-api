package com.medichub.dto.response;

public record AdminMetricsResponse(
        long totalInstructors,
        long totalStudents,
        long totalTests,
        long activeSubscriptions
) {
}
