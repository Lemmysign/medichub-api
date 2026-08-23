package com.medichub.dto.response;

import java.time.Instant;
import java.util.List;

/** Revenue (in kobo) over [from, to), bucketed by day/week/month. */
public record RevenueReportResponse(
        String granularity,
        Instant from,
        Instant to,
        long totalKobo,
        List<RevenueBucket> buckets
) {
    public record RevenueBucket(Instant periodStart, long amountKobo) {
    }
}
