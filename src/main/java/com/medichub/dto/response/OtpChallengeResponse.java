package com.medichub.dto.response;

/**
 * Returned by register/resend: the account exists but is unverified. The SPA takes the user to the
 * OTP screen (no tokens are issued yet). {@code email} is echoed back so the screen can prefill it.
 */
public record OtpChallengeResponse(
        String email,
        String message
) {
}
