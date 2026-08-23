package com.medichub.service.impl;

import com.medichub.dto.paystack.PaystackDTO;
import com.medichub.exception.ResourceNotFoundException;
import com.medichub.model.Payment;
import com.medichub.model.Subscription;
import com.medichub.model.SubscriptionPlan;
import com.medichub.model.User;
import com.medichub.model.enums.PaymentStatus;
import com.medichub.model.enums.SubscriptionStatus;
import com.medichub.repository.PaymentRepository;
import com.medichub.repository.SubscriptionPlanRepository;
import com.medichub.repository.SubscriptionRepository;
import com.medichub.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Database-mutating half of the payment flow, in its own bean so Spring's
 * {@code @Transactional} proxy is actually applied (adapted from the Cafeteria
 * backend's PaymentBalanceService — which documents that reaching these via
 * internal {@code this.} calls silently bypasses the transaction boundary).
 *
 * <p>The orchestrator ({@code SubscriptionService}) is non-transactional and calls
 * into this bean, so each DB mutation runs in its own short transaction.
 */
@Service
public class SubscriptionPaymentService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionPaymentService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @PersistenceContext
    private EntityManager entityManager;

    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;

    public SubscriptionPaymentService(PaymentRepository paymentRepository,
                                      SubscriptionRepository subscriptionRepository,
                                      SubscriptionPlanRepository subscriptionPlanRepository,
                                      UserRepository userRepository) {
        this.paymentRepository = paymentRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userRepository = userRepository;
    }

    /** Creates the PENDING payment (with a unique reference) in its own transaction. */
    @Transactional
    public Payment createPending(Long studentId, SubscriptionPlan plan) {
        byte[] randomBytes = new byte[8];
        SECURE_RANDOM.nextBytes(randomBytes);
        String reference = "MHUB_" + HexFormat.of().formatHex(randomBytes);

        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        Payment payment = new Payment();
        payment.setStudent(student);
        payment.setStudentName(student.getFullName());
        payment.setStudentEmail(student.getEmail());
        payment.setAmountKobo(plan.getPriceKobo());
        payment.setCurrency(plan.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaystackReference(reference);
        return paymentRepository.save(payment);
    }

    /**
     * Marks a verified transaction SUCCESS and activates/renews the subscription.
     * Idempotent (double-checked under a pessimistic lock) and refuses to credit
     * unless Paystack actually collected at least the amount we charged.
     */
    @Transactional
    public boolean activate(String reference, PaystackDTO.VerifyResponse verify) {
        Payment payment = paymentRepository.findByPaystackReference(reference).orElse(null);
        if (payment == null) {
            log.warn("No payment found for reference {}", reference);
            return false;
        }
        if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            return true; // already processed
        }
        if (!isPaidAmountValid(payment, verify)) {
            log.error("Amount mismatch for reference {} — expected >= {} kobo. Not activating.",
                    reference, payment.getAmountKobo());
            return false;
        }

        // Lock the payment row to serialize concurrent webhook + callback activation.
        payment = entityManager.find(Payment.class, payment.getId(), LockModeType.PESSIMISTIC_WRITE);
        if (PaymentStatus.SUCCESS.equals(payment.getStatus())) {
            return true; // completed by another thread while we waited for the lock
        }

        SubscriptionPlan plan = subscriptionPlanRepository.findFirstByActiveTrueOrderByIdAsc().orElse(null);
        if (plan == null) {
            log.error("No active subscription plan configured; cannot activate reference {}", reference);
            return false;
        }

        Instant now = Instant.now();
        Subscription subscription = subscriptionRepository
                .findFirstByStudentIdOrderByCreatedAtDesc(payment.getStudent().getId())
                .orElse(null);

        // Renewal extends from the current end date if still active; otherwise from now.
        Instant base = (subscription != null
                && SubscriptionStatus.ACTIVE.equals(subscription.getStatus())
                && subscription.getEndDate() != null
                && subscription.getEndDate().isAfter(now))
                ? subscription.getEndDate()
                : now;
        Instant newEnd = base.plus(Duration.ofDays(plan.getIntervalDays()));

        if (subscription == null) {
            subscription = new Subscription();
            subscription.setStudent(payment.getStudent());
            subscription.setStartDate(now);
        } else if (subscription.getStartDate() == null) {
            subscription.setStartDate(now);
        }
        // Denormalized names for readable rows (carried from the payment snapshot).
        subscription.setStudentName(payment.getStudentName());
        subscription.setStudentEmail(payment.getStudentEmail());
        subscription.setPlan(plan);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setEndDate(newEnd);
        if (verify.getData() != null && verify.getData().getCustomer() != null) {
            subscription.setPaystackCustomerCode(verify.getData().getCustomer().getCustomerCode());
        }
        subscription = subscriptionRepository.save(subscription);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(now);
        payment.setSubscription(subscription);
        entityManager.flush();

        log.info("Activated subscription for student {} via reference {} (endDate={})",
                payment.getStudent().getId(), reference, newEnd);
        return true;
    }

    private boolean isPaidAmountValid(Payment payment, PaystackDTO.VerifyResponse verify) {
        if (verify == null || verify.getData() == null || verify.getData().getAmount() == null) {
            return false;
        }
        BigDecimal paidKobo = verify.getData().getAmount();
        return paidKobo.compareTo(BigDecimal.valueOf(payment.getAmountKobo())) >= 0;
    }
}
