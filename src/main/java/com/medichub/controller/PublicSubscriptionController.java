package com.medichub.controller;

import com.medichub.config.PaystackProperties;
import com.medichub.dto.response.SubscriptionPlanResponse;
import com.medichub.service.SubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Public pricing + Paystack public key for the SPA checkout (CLAUDE.md §9). */
@RestController
@RequestMapping("/api/public")
public class PublicSubscriptionController {

    private final SubscriptionService subscriptionService;
    private final PaystackProperties paystackProperties;

    public PublicSubscriptionController(SubscriptionService subscriptionService,
                                        PaystackProperties paystackProperties) {
        this.subscriptionService = subscriptionService;
        this.paystackProperties = paystackProperties;
    }

    @GetMapping("/subscription-plan")
    public SubscriptionPlanResponse plan() {
        return subscriptionService.getActivePlan();
    }

    @GetMapping("/payment-config")
    public Map<String, String> paymentConfig() {
        return Map.of("publicKey", paystackProperties.publicKey() == null ? "" : paystackProperties.publicKey());
    }
}
