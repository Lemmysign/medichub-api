package com.medichub.service.impl;

import com.medichub.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String frontendUrl;
    /** Dev convenience: when true, log OTP codes so they can be read from the console without SMTP. */
    private final boolean logOtpCodes;

    public EmailServiceImpl(JavaMailSender mailSender,
                            @Value("${app.mail.from}") String fromAddress,
                            @Value("${app.frontend.url}") String frontendUrl,
                            @Value("${app.otp.log-codes:false}") boolean logOtpCodes) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.frontendUrl = frontendUrl;
        this.logOtpCodes = logOtpCodes;
    }

    /**
     * Sent asynchronously so the HTTP request is not blocked on SMTP, and so a mail
     * outage never leaks account-existence information via response timing. Failures
     * are logged, not surfaced to the caller.
     */
    @Override
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Reset your MedicHub Academy password");
            message.setText("""
                    We received a request to reset your MedicHub Academy password.

                    Click the link below to choose a new password. This link expires in 30 minutes.
                    %s

                    If you did not request this, you can safely ignore this email.
                    """.formatted(resetLink));
            mailSender.send(message);
            log.info("Password reset email sent to {}", toEmail);
        } catch (MailException ex) {
            log.warn("Failed to send password reset email to {}: {}", toEmail, ex.getMessage());
        }
    }

    @Override
    @Async
    public void sendOtpEmail(String toEmail, String code) {
        // Dev aid: with no SMTP configured, this lets you read the code from the server console.
        if (logOtpCodes) {
            log.info("OTP for {} is {}", toEmail, code);
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(toEmail);
            message.setSubject("Your MedicHub Academy verification code");
            message.setText("""
                    Welcome to MedicHub Academy!

                    Your email verification code is: %s

                    Enter it on the verification screen to activate your account. This code expires in 10 minutes.

                    If you did not create an account, you can safely ignore this email.
                    """.formatted(code));
            mailSender.send(message);
            log.info("OTP email sent to {}", toEmail);
        } catch (MailException ex) {
            log.warn("Failed to send OTP email to {}: {}", toEmail, ex.getMessage());
        }
    }
}
