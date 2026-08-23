package com.medichub.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Admin sets the plan in plain Naira (e.g. 10000 = ₦10,000); the service converts
 * to kobo for storage. {@code currency} is optional (defaults to NGN).
 */
public record UpsertSubscriptionPlanRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @DecimalMin(value = "1.0", message = "Price must be at least ₦1") BigDecimal priceNaira,
        @NotNull @Min(value = 1, message = "Interval must be at least 1 day") Integer intervalDays,
        @Size(max = 3) String currency
) {
}
