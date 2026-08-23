package com.medichub.dto.response;

import com.medichub.model.enums.SubscriptionStatus;

import java.time.Instant;

/** A student's current subscription state. {@code active} is the effective gate result. */
public record SubscriptionStatusResponse(
        boolean active,
        SubscriptionStatus status,
        String planName,
        Instant startDate,
        Instant endDate
) {
    public static SubscriptionStatusResponse none() {
        return new SubscriptionStatusResponse(false, null, null, null, null);
    }
}
