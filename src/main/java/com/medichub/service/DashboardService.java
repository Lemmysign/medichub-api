package com.medichub.service;

import com.medichub.dto.response.InstructorDashboardResponse;
import com.medichub.dto.response.StudentDashboardResponse;

public interface DashboardService {

    InstructorDashboardResponse getInstructorDashboard();

    StudentDashboardResponse getStudentDashboard();
}
