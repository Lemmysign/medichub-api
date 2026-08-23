package com.medichub.service;

import com.medichub.dto.request.ForgotPasswordRequest;
import com.medichub.dto.request.LoginRequest;
import com.medichub.dto.request.RegisterRequest;
import com.medichub.dto.request.ResetPasswordRequest;
import com.medichub.dto.response.AuthResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
