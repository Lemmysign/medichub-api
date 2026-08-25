package com.medichub.model;

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

    /** BCrypt-hashed password (email/password is the only auth method). */
    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    /**
     * Email confirmed via the 6-digit OTP. New sign-ups start false and cannot log in until verified.
     * Column default true so accounts that existed before this feature are grandfathered as verified.
     */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean emailVerified = false;

    /**
     * Admin approval gate for instructors. Students/admins are always approved; a new instructor
     * starts false and cannot log in until an admin approves. Column default true grandfathers
     * pre-existing accounts.
     */
    @Column(nullable = false, columnDefinition = "boolean default true")
    private boolean approved = true;
}
