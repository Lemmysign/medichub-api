package com.medichub.service;

import com.medichub.dto.response.RevenueReportResponse;
import com.medichub.dto.response.RevenueReportResponse.RevenueBucket;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Assembles a {@link RevenueReportResponse} from grouped DB rows ([bucketStart, sumKobo]),
 * summing the buckets into the grand total. Pure logic — unit-testable without a DB.
 */
public final class RevenueReportAssembler {

    private RevenueReportAssembler() {
    }

    public static RevenueReportResponse assemble(String granularity, Instant from, Instant to, List<Object[]> rows) {
        List<RevenueBucket> buckets = new ArrayList<>(rows.size());
        long total = 0L;
        for (Object[] row : rows) {
            Instant periodStart = toInstant(row[0]);
            long amount = ((Number) row[1]).longValue();
            buckets.add(new RevenueBucket(periodStart, amount));
            total += amount;
        }
        return new RevenueReportResponse(granularity, from, to, total, buckets);
    }

    private static Instant toInstant(Object value) {
        return switch (value) {
            case Instant i -> i;
            case Timestamp t -> t.toInstant();
            case OffsetDateTime odt -> odt.toInstant();
            case LocalDateTime ldt -> ldt.toInstant(ZoneOffset.UTC);
            case null -> null;
            default -> throw new IllegalArgumentException("Unsupported temporal type: " + value.getClass());
        };
    }
}
