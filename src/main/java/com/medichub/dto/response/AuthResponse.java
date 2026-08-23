package com.medichub.dto.response;

/**
 * Issued on register / login / refresh. Carries the JWT pair plus basic user info
 * so the SPA can render immediately without a follow-up call.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresInSeconds,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken, long expiresInSeconds, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", expiresInSeconds, user);
    }
}
