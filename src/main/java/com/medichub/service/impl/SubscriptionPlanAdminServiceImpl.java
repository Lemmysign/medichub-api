package com.medichub.service.impl;

import com.medichub.dto.request.UpsertSubscriptionPlanRequest;
import com.medichub.dto.response.AdminSubscriptionPlanResponse;
import com.medichub.model.SubscriptionPlan;
import com.medichub.repository.SubscriptionPlanRepository;
import com.medichub.service.SubscriptionPlanAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@Transactional(readOnly = true)
public class SubscriptionPlanAdminServiceImpl implements SubscriptionPlanAdminService {

    private static final BigDecimal KOBO_PER_NAIRA = BigDecimal.valueOf(100);

    private final SubscriptionPlanRepository planRepository;

    public SubscriptionPlanAdminServiceImpl(SubscriptionPlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public AdminSubscriptionPlanResponse getPlan() {
        return existingPlan()
                .map(this::toResponse)
                .orElseGet(() -> new AdminSubscriptionPlanResponse(
                        false, null, null, null, null, "NGN", 30, false));
    }

    @Override
    @Transactional
    public AdminSubscriptionPlanResponse upsertPlan(UpsertSubscriptionPlanRequest request) {
        SubscriptionPlan plan = existingPlan().orElseGet(SubscriptionPlan::new);
        plan.setName(request.name().trim());
        plan.setPriceKobo(nairaToKobo(request.priceNaira()));
        plan.setCurrency(request.currency() == null || request.currency().isBlank()
                ? "NGN" : request.currency().trim().toUpperCase());
        plan.setIntervalDays(request.intervalDays());
        plan.setActive(true);
        plan = planRepository.save(plan);
        return toResponse(plan);
    }

    // Keep a single plan row: reuse the first one that exists (active or not), else create.
    private java.util.Optional<SubscriptionPlan> existingPlan() {
        return planRepository.findAll().stream().findFirst();
    }

    private long nairaToKobo(BigDecimal naira) {
        return naira.multiply(KOBO_PER_NAIRA).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }

    private BigDecimal koboToNaira(long kobo) {
        return BigDecimal.valueOf(kobo).divide(KOBO_PER_NAIRA, 2, RoundingMode.HALF_UP);
    }

    private AdminSubscriptionPlanResponse toResponse(SubscriptionPlan plan) {
        return new AdminSubscriptionPlanResponse(
                true,
                plan.getId(),
                plan.getName(),
                plan.getPriceKobo(),
                koboToNaira(plan.getPriceKobo()),
                plan.getCurrency(),
                plan.getIntervalDays(),
                plan.isActive());
    }
}
