package com.medichub.dto.response;

/** Returned when a student starts checkout — the SPA redirects to authorizationUrl. */
public record InitializeSubscriptionResponse(
        String authorizationUrl,
        String accessCode,
        String reference
) {
}
