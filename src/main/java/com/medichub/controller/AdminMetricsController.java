package com.medichub.controller;

import com.medichub.dto.response.AdminMetricsResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.RevenueReportResponse;
import com.medichub.dto.response.SubscriberResponse;
import com.medichub.service.AdminMetricsService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/admin")
public class AdminMetricsController {

    private final AdminMetricsService adminMetricsService;

    public AdminMetricsController(AdminMetricsService adminMetricsService) {
        this.adminMetricsService = adminMetricsService;
    }

    @GetMapping("/metrics")
    public AdminMetricsResponse metrics() {
        return adminMetricsService.getPlatformMetrics();
    }

    @GetMapping("/metrics/revenue")
    public RevenueReportResponse revenue(
            @RequestParam(defaultValue = "day") String granularity,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return adminMetricsService.getRevenueReport(granularity, from, to);
    }

    @GetMapping("/subscribers")
    public PagedResponse<SubscriberResponse> subscribers(@PageableDefault(size = 20) Pageable pageable) {
        return adminMetricsService.listActiveSubscribers(pageable);
    }
}
