package com.medichub.dto.response;

import java.time.Instant;

/** An instructor awaiting admin approval, for the admin approvals queue. */
public record PendingInstructorResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        boolean emailVerified,
        Instant registeredAt
) {
}
