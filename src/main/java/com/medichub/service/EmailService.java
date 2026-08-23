package com.medichub.service;

public interface EmailService {

    /** Send a password-reset email containing the reset link/token to the user. */
    void sendPasswordResetEmail(String toEmail, String resetToken);
}
