package com.medichub.dto.response;

public record InstructorDashboardResponse(
        long totalCourses,
        long totalStudentsEnrolled,
        long totalTests,
        long totalStudentsTested
) {
}
