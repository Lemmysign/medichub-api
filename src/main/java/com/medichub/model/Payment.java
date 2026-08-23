package com.medichub.model;

import com.medichub.model.enums.PaymentStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Every charge, for revenue analytics. Amount in kobo (CLAUDE.md §4, §5.4).
 * {@code paystackReference} is unique — the hard idempotency backstop for webhooks.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments",
        uniqueConstraints = @UniqueConstraint(name = "uk_payments_reference", columnNames = "paystackReference"),
        indexes = {
                @Index(name = "idx_payments_student", columnList = "student_id"),
                @Index(name = "idx_payments_status_paidat", columnList = "status, paidAt")
        })
public class Payment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /** Snapshot of the payer's name/email for readable, audit-friendly rows. */
    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_email")
    private String studentEmail;

    /** Nullable: a charge may exist before/independently of a subscription record. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id")
    private Subscription subscription;

    @Column(nullable = false)
    private Long amountKobo;

    @Column(nullable = false, length = 3)
    private String currency = "NGN";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    private String paystackReference;

    private Instant paidAt;
}
