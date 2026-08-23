package com.medichub.controller;

import com.medichub.dto.request.UpsertSubscriptionPlanRequest;
import com.medichub.dto.response.AdminSubscriptionPlanResponse;
import com.medichub.service.SubscriptionPlanAdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin manages the subscription plan price/interval (Naira in, kobo stored). */
@RestController
@RequestMapping("/api/admin/subscription-plan")
public class AdminSubscriptionPlanController {

    private final SubscriptionPlanAdminService subscriptionPlanAdminService;

    public AdminSubscriptionPlanController(SubscriptionPlanAdminService subscriptionPlanAdminService) {
        this.subscriptionPlanAdminService = subscriptionPlanAdminService;
    }

    @GetMapping
    public AdminSubscriptionPlanResponse getPlan() {
        return subscriptionPlanAdminService.getPlan();
    }

    @PutMapping
    public AdminSubscriptionPlanResponse upsertPlan(@Valid @RequestBody UpsertSubscriptionPlanRequest request) {
        return subscriptionPlanAdminService.upsertPlan(request);
    }
}
