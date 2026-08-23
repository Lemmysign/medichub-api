package com.medichub.model;

import com.medichub.model.enums.AuthProvider;
import com.medichub.model.enums.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * One table for all roles (CLAUDE.md §4, §12). Admin is granted when the email
 * matches {@code app.admin.email}; there is no admin self-signup.
 */
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
public class User extends BaseEntity {

    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;

    /** Null for Google-only accounts. BCrypt-hashed for LOCAL accounts. */
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider authProvider;

    /** Provider's subject id (e.g. Google sub); null for LOCAL accounts. */
    private String providerId;

    @Column(nullable = false)
    private boolean enabled = true;
}
