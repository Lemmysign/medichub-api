package com.medichub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * A subscription plan. MVP ships one active plan; money is stored in kobo (CLAUDE.md §4).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {

    @Column(nullable = false)
    private String name;

    /** Price in kobo (smallest Naira unit). */
    @Column(nullable = false)
    private Long priceKobo;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Column(nullable = false)
    private int intervalDays;

    private String paystackPlanCode;

    @Column(nullable = false)
    private boolean active = true;
}
