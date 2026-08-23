package com.medichub.dto.request;

import jakarta.validation.constraints.NotBlank;

/** Used by both /refresh and /logout. */
public record RefreshTokenRequest(
        @NotBlank String refreshToken
) {
}
