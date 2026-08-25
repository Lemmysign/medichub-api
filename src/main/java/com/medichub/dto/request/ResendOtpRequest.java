package com.medichub.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** Ask for a fresh 6-digit verification code to be emailed. */
public record ResendOtpRequest(
        @NotBlank @Email String email
) {
}
