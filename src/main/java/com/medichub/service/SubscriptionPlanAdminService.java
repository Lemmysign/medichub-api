package com.medichub.service;

import com.medichub.dto.request.UpsertSubscriptionPlanRequest;
import com.medichub.dto.response.AdminSubscriptionPlanResponse;

/** Admin management of the single subscription plan (CLAUDE.md §5.4). */
public interface SubscriptionPlanAdminService {

    /** Current plan for the admin form; {@code configured=false} if none exists yet. */
    AdminSubscriptionPlanResponse getPlan();

    /** Create the plan if none exists, otherwise update it. Naira in, kobo stored. */
    AdminSubscriptionPlanResponse upsertPlan(UpsertSubscriptionPlanRequest request);
}
