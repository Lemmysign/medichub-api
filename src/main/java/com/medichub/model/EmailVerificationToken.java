package com.medichub.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A single-use 6-digit email verification code (OTP) issued at registration. The code itself is
 * never stored in plaintext — only its BCrypt hash — and it is short-lived with a capped number of
 * verification attempts.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "email_verification_tokens",
        indexes = @Index(name = "idx_email_verification_tokens_user", columnList = "user_id"))
public class EmailVerificationToken extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** BCrypt hash of the 6-digit code. */
    @Column(nullable = false)
    private String codeHash;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    /** Failed verification attempts against this code; the code is burned once it hits the cap. */
    @Column(nullable = false)
    private int attempts = 0;
}
