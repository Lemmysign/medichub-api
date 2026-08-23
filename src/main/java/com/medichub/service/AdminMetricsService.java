package com.medichub.service;

import com.medichub.dto.response.AdminMetricsResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.RevenueReportResponse;
import com.medichub.dto.response.SubscriberResponse;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface AdminMetricsService {

    AdminMetricsResponse getPlatformMetrics();

    /** Revenue over [from,to) bucketed by granularity (day/week/month). Nulls default sensibly. */
    RevenueReportResponse getRevenueReport(String granularity, Instant from, Instant to);

    PagedResponse<SubscriberResponse> listActiveSubscribers(Pageable pageable);
}
