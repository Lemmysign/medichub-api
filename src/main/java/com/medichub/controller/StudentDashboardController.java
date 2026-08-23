package com.medichub.controller;

import com.medichub.dto.response.StudentDashboardResponse;
import com.medichub.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/dashboard")
public class StudentDashboardController {

    private final DashboardService dashboardService;

    public StudentDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public StudentDashboardResponse dashboard() {
        return dashboardService.getStudentDashboard();
    }
}
