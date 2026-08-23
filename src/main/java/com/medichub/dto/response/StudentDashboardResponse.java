package com.medichub.dto.response;

public record StudentDashboardResponse(
        long coursesEnrolled,
        long testsTaken,
        int averageScorePercent,
        long coursesCompleted
) {
}
