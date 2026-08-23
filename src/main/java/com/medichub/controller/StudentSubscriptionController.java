package com.medichub.controller;

import com.medichub.dto.response.InitializeSubscriptionResponse;
import com.medichub.dto.response.SubscriptionStatusResponse;
import com.medichub.security.SecurityUtils;
import com.medichub.service.SubscriptionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/subscription")
public class StudentSubscriptionController {

    private final SubscriptionService subscriptionService;

    public StudentSubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    /** Start checkout — returns the Paystack authorization URL for the SPA to redirect to. */
    @PostMapping("/initialize")
    public InitializeSubscriptionResponse initialize() {
        return subscriptionService.initializeSubscription(SecurityUtils.currentUserId());
    }

    /** Called from the SPA callback to confirm a reference; returns the fresh status. */
    @GetMapping("/verify/{reference}")
    public SubscriptionStatusResponse verify(@PathVariable String reference) {
        subscriptionService.verifyAndActivate(reference);
        return subscriptionService.getMySubscription(SecurityUtils.currentUserId());
    }

    @GetMapping
    public SubscriptionStatusResponse mySubscription() {
        return subscriptionService.getMySubscription(SecurityUtils.currentUserId());
    }
}
