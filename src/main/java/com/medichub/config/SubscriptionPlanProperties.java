package com.medichub.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The single subscription plan, seeded on startup (CLAUDE.md §6, MVP). Price is
 * intentionally not defaulted — it must be supplied via env so it is never
 * hardcoded. Bound from {@code app.subscription.plan.*}.
 */
@ConfigurationProperties(prefix = "app.subscription.plan")
public record SubscriptionPlanProperties(
        String name,
        Long priceKobo,
        String currency,
        Integer intervalDays
) {
}
