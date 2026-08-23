package com.medichub.service.impl;

import com.medichub.dto.paystack.PaystackDTO;
import com.medichub.dto.response.InitializeSubscriptionResponse;
import com.medichub.dto.response.SubscriptionPlanResponse;
import com.medichub.dto.response.SubscriptionStatusResponse;
import com.medichub.exception.BadRequestException;
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
import com.medichub.service.PaystackClient;
import com.medichub.service.SubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(SubscriptionServiceImpl.class);
    private static final String EVENT_CHARGE_SUCCESS = "charge.success";
    private static final String PAYSTACK_STATUS_SUCCESS = "success";

    private final PaystackClient paystackClient;
    private final SubscriptionPaymentService subscriptionPaymentService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;

    public SubscriptionServiceImpl(PaystackClient paystackClient,
                                   SubscriptionPaymentService subscriptionPaymentService,
                                   SubscriptionPlanRepository subscriptionPlanRepository,
                                   SubscriptionRepository subscriptionRepository,
                                   PaymentRepository paymentRepository,
                                   UserRepository userRepository) {
        this.paystackClient = paystackClient;
        this.subscriptionPaymentService = subscriptionPaymentService;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
    }

    @Override
    public InitializeSubscriptionResponse initializeSubscription(Long studentId) {
        SubscriptionPlan plan = requireActivePlan();
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));

        // Persist a PENDING payment (own transaction) before the external API call.
        Payment pending = subscriptionPaymentService.createPending(studentId, plan);

        PaystackDTO.InitializeResponse response = paystackClient.initialize(
                pending.getPaystackReference(), plan.getPriceKobo(), student.getEmail(), plan.getCurrency());

        if (response == null || !response.isStatus() || response.getData() == null) {
            throw new BadRequestException("Could not start payment. Please try again.");
        }
        PaystackDTO.InitializeResponse.Data data = response.getData();
        return new InitializeSubscriptionResponse(
                data.getAuthorizationUrl(), data.getAccessCode(), pending.getPaystackReference());
    }

    @Override
    public boolean verifyAndActivate(String reference) {
        PaystackDTO.VerifyResponse response = paystackClient.verify(reference);
        if (response == null || !response.isStatus() || response.getData() == null) {
            return false;
        }
        if (!PAYSTACK_STATUS_SUCCESS.equalsIgnoreCase(response.getData().getStatus())) {
            return false;
        }
        return subscriptionPaymentService.activate(reference, response);
    }

    @Override
    public boolean processWebhookEvent(PaystackDTO.WebhookEvent event) {
        if (event == null || !EVENT_CHARGE_SUCCESS.equals(event.getEvent())
                || event.getData() == null || event.getData().getReference() == null) {
            return false;
        }
        String reference = event.getData().getReference();

        // Idempotency short-circuit before any external call.
        if (paymentRepository.countByPaystackReferenceAndStatus(reference, PaymentStatus.SUCCESS) > 0) {
            log.info("Webhook for reference {} already processed, skipping", reference);
            return true;
        }
        // Never trust webhook amounts — re-verify with Paystack, then activate.
        return verifyAndActivate(reference);
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionStatusResponse getMySubscription(Long studentId) {
        Subscription sub = subscriptionRepository.findFirstByStudentIdOrderByCreatedAtDesc(studentId).orElse(null);
        if (sub == null) {
            return SubscriptionStatusResponse.none();
        }
        boolean active = SubscriptionStatus.ACTIVE.equals(sub.getStatus())
                && sub.getEndDate() != null && sub.getEndDate().isAfter(Instant.now());
        String planName = sub.getPlan() == null ? null : sub.getPlan().getName();
        return new SubscriptionStatusResponse(active, sub.getStatus(), planName, sub.getStartDate(), sub.getEndDate());
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionPlanResponse getActivePlan() {
        SubscriptionPlan plan = requireActivePlan();
        return new SubscriptionPlanResponse(
                plan.getId(), plan.getName(), plan.getPriceKobo(), plan.getCurrency(), plan.getIntervalDays());
    }

    private SubscriptionPlan requireActivePlan() {
        return subscriptionPlanRepository.findFirstByActiveTrueOrderByIdAsc()
                .orElseThrow(() -> new BadRequestException("No subscription plan is configured yet"));
    }
}
