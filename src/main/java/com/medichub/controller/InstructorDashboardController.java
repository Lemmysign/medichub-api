package com.medichub.controller;

import com.medichub.dto.response.InstructorDashboardResponse;
import com.medichub.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/instructor/dashboard")
public class InstructorDashboardController {

    private final DashboardService dashboardService;

    public InstructorDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public InstructorDashboardResponse dashboard() {
        return dashboardService.getInstructorDashboard();
    }
}
