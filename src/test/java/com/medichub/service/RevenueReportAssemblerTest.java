package com.medichub.service;

import com.medichub.dto.response.RevenueReportResponse;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RevenueReportAssemblerTest {

    private static Object[] row(Instant start, long amount) {
        return new Object[]{Timestamp.from(start), amount};
    }

    @Test
    void sumsBucketsIntoTotal() {
        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-08-04T00:00:00Z");
        List<Object[]> rows = List.of(
                row(Instant.parse("2026-08-01T00:00:00Z"), 50_000L),
                row(Instant.parse("2026-08-02T00:00:00Z"), 30_000L),
                row(Instant.parse("2026-08-03T00:00:00Z"), 20_000L));

        RevenueReportResponse report = RevenueReportAssembler.assemble("day", from, to, rows);

        assertThat(report.granularity()).isEqualTo("day");
        assertThat(report.from()).isEqualTo(from);
        assertThat(report.to()).isEqualTo(to);
        assertThat(report.totalKobo()).isEqualTo(100_000L);
        assertThat(report.buckets()).hasSize(3);
        assertThat(report.buckets().get(0).amountKobo()).isEqualTo(50_000L);
        assertThat(report.buckets().get(0).periodStart()).isEqualTo(Instant.parse("2026-08-01T00:00:00Z"));
    }

    @Test
    void emptyRowsGivesZeroTotal() {
        Instant from = Instant.parse("2026-08-01T00:00:00Z");
        Instant to = Instant.parse("2026-09-01T00:00:00Z");

        RevenueReportResponse report = RevenueReportAssembler.assemble("month", from, to, List.of());

        assertThat(report.totalKobo()).isZero();
        assertThat(report.buckets()).isEmpty();
    }

    @Test
    void acceptsInstantRowsToo() {
        Instant start = Instant.parse("2026-08-01T00:00:00Z");
        RevenueReportResponse report = RevenueReportAssembler.assemble(
                "day", start, start.plusSeconds(86400),
                List.<Object[]>of(new Object[]{start, 12_345L}));

        assertThat(report.totalKobo()).isEqualTo(12_345L);
        assertThat(report.buckets().get(0).periodStart()).isEqualTo(start);
    }
}
