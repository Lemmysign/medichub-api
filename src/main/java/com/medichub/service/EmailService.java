package com.medichub.service;

public interface EmailService {

    /** Send a password-reset email containing the reset link/token to the user. */
    void sendPasswordResetEmail(String toEmail, String resetToken);

    /** Send the 6-digit email-verification code to a newly registered user. */
    void sendOtpEmail(String toEmail, String code);
}
