package com.medichub.dto.request;

import com.medichub.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Signup payload. {@code role} may only be STUDENT or INSTRUCTOR — the service
 * rejects ADMIN here; admin is granted solely by matching app.admin.email.
 */
public record RegisterRequest(
        @NotBlank @Size(max = 120) String fullName,
        @NotBlank @Email @Size(max = 180) String email,
        @Size(max = 30) String phone,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull Role role
) {
}
