package com.medichub.dto.response;

/**
 * Result of confirming the OTP.
 * <ul>
 *   <li>Student/admin (or an already-approved instructor): {@code auth} carries the JWT pair — the
 *       SPA logs them straight in.</li>
 *   <li>A freshly-verified instructor awaiting approval: {@code pendingApproval = true} and
 *       {@code auth = null} — the SPA shows the "awaiting approval" screen.</li>
 * </ul>
 */
public record VerifyOtpResponse(
        boolean pendingApproval,
        AuthResponse auth
) {
    public static VerifyOtpResponse loggedIn(AuthResponse auth) {
        return new VerifyOtpResponse(false, auth);
    }

    public static VerifyOtpResponse pending() {
        return new VerifyOtpResponse(true, null);
    }
}
