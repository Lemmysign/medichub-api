package com.medichub.dto.response;

import com.medichub.model.enums.Role;

/** Non-sensitive user projection (never exposes the password hash). */
public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role,
        boolean enabled
) {
}
