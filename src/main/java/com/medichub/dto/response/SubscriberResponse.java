package com.medichub.dto.response;

import com.medichub.model.enums.SubscriptionStatus;

import java.time.Instant;

public record SubscriberResponse(
        Long studentId,
        String fullName,
        String email,
        SubscriptionStatus status,
        String planName,
        Instant startDate,
        Instant endDate
) {
}
