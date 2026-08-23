package com.medichub.dto.response;

import java.math.BigDecimal;

/**
 * Admin view of the subscription plan: kobo (source of truth) plus a Naira value
 * for display. {@code configured} is false when no plan exists yet.
 */
public record AdminSubscriptionPlanResponse(
        boolean configured,
        Long id,
        String name,
        Long priceKobo,
        BigDecimal priceNaira,
        String currency,
        Integer intervalDays,
        boolean active
) {
}
