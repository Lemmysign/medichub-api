package com.medichub.service;

import com.medichub.dto.paystack.PaystackDTO;
import com.medichub.dto.response.InitializeSubscriptionResponse;
import com.medichub.dto.response.SubscriptionPlanResponse;
import com.medichub.dto.response.SubscriptionStatusResponse;

public interface SubscriptionService {

    /** Start checkout for the current student — returns the Paystack authorization URL. */
    InitializeSubscriptionResponse initializeSubscription(Long studentId);

    /** Verify a reference with Paystack and activate/renew if successful (idempotent). */
    boolean verifyAndActivate(String reference);

    /** Handle a (already signature-verified) Paystack webhook event. */
    boolean processWebhookEvent(PaystackDTO.WebhookEvent event);

    SubscriptionStatusResponse getMySubscription(Long studentId);

    SubscriptionPlanResponse getActivePlan();
}
