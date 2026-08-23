package com.medichub.service.impl;

import com.medichub.dto.response.AdminMetricsResponse;
import com.medichub.dto.response.PagedResponse;
import com.medichub.dto.response.RevenueReportResponse;
import com.medichub.dto.response.SubscriberResponse;
import com.medichub.exception.BadRequestException;
import com.medichub.model.Subscription;
import com.medichub.model.enums.Role;
import com.medichub.model.enums.SubscriptionStatus;
import com.medichub.repository.PaymentRepository;
import com.medichub.repository.SubscriptionRepository;
import com.medichub.repository.TestRepository;
import com.medichub.repository.UserRepository;
import com.medichub.service.AdminMetricsService;
import com.medichub.service.RevenueReportAssembler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

@Service
@Transactional(readOnly = true)
public class AdminMetricsServiceImpl implements AdminMetricsService {

    private static final Set<String> ALLOWED_GRANULARITIES = Set.of("day", "week", "month");

    private final UserRepository userRepository;
    private final TestRepository testRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;

    public AdminMetricsServiceImpl(UserRepository userRepository,
                                   TestRepository testRepository,
                                   SubscriptionRepository subscriptionRepository,
                                   PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.testRepository = testRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public AdminMetricsResponse getPlatformMetrics() {
        return new AdminMetricsResponse(
                userRepository.countByRole(Role.INSTRUCTOR),
                userRepository.countByRole(Role.STUDENT),
                testRepository.count(),
                subscriptionRepository.countByStatusAndEndDateAfter(SubscriptionStatus.ACTIVE, Instant.now()));
    }

    @Override
    public RevenueReportResponse getRevenueReport(String granularity, Instant from, Instant to) {
        String bucket = granularity == null ? "day" : granularity.toLowerCase();
        if (!ALLOWED_GRANULARITIES.contains(bucket)) {
            throw new BadRequestException("granularity must be one of day, week, month");
        }
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null ? from : defaultFrom(bucket, resolvedTo);
        if (!resolvedFrom.isBefore(resolvedTo)) {
            throw new BadRequestException("'from' must be before 'to'");
        }
        return RevenueReportAssembler.assemble(bucket, resolvedFrom, resolvedTo,
                paymentRepository.revenueSeries(bucket, resolvedFrom, resolvedTo));
    }

    @Override
    public PagedResponse<SubscriberResponse> listActiveSubscribers(Pageable pageable) {
        Page<Subscription> page = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE, pageable);
        return PagedResponse.from(page, s -> new SubscriberResponse(
                s.getStudent().getId(),
                s.getStudent().getFullName(),
                s.getStudent().getEmail(),
                s.getStatus(),
                s.getPlan() == null ? null : s.getPlan().getName(),
                s.getStartDate(),
                s.getEndDate()));
    }

    private Instant defaultFrom(String granularity, Instant to) {
        return switch (granularity) {
            case "week" -> to.minus(84, ChronoUnit.DAYS);   // ~12 weeks
            case "month" -> to.minus(365, ChronoUnit.DAYS);  // ~12 months
            default -> to.minus(30, ChronoUnit.DAYS);        // day
        };
    }
}
