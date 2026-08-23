package com.medichub.dto.response;

/** Public plan details for the pricing page. Amount in kobo. */
public record SubscriptionPlanResponse(
        Long id,
        String name,
        Long priceKobo,
        String currency,
        int intervalDays
) {
}
