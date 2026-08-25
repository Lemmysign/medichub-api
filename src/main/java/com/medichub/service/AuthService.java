package com.medichub.service;

import com.medichub.dto.request.ForgotPasswordRequest;
import com.medichub.dto.request.LoginRequest;
import com.medichub.dto.request.RegisterRequest;
import com.medichub.dto.request.ResendOtpRequest;
import com.medichub.dto.request.ResetPasswordRequest;
import com.medichub.dto.request.VerifyOtpRequest;
import com.medichub.dto.response.AuthResponse;
import com.medichub.dto.response.OtpChallengeResponse;
import com.medichub.dto.response.VerifyOtpResponse;

public interface AuthService {

    /** Creates an unverified account and emails a 6-digit OTP. No tokens until the OTP is confirmed. */
    OtpChallengeResponse register(RegisterRequest request);

    /** Confirms the OTP: students/admins are logged in; unapproved instructors get a pending result. */
    VerifyOtpResponse verifyOtp(VerifyOtpRequest request);

    /** Re-issues a fresh OTP for an unverified account. Always succeeds (never reveals existence). */
    OtpChallengeResponse resendOtp(ResendOtpRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
