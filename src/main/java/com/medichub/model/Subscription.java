package com.medichub.model;

import com.medichub.model.enums.SubscriptionStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A student's subscription. Platform access requires status = ACTIVE and endDate
 * in the future (CLAUDE.md §4, §6).
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscriptions", indexes = {
        @Index(name = "idx_subscriptions_student_status", columnList = "student_id, status"),
        @Index(name = "idx_subscriptions_paystack_sub", columnList = "paystackSubscriptionCode")
})
public class Subscription extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Denormalized copy of the student's name/email for readable rows (kept in sync on rename). */
    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_email")
    private String studentEmail;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status;

    private Instant startDate;

    private Instant endDate;

    private String paystackSubscriptionCode;

    private String paystackCustomerCode;
}
