package com.medichub.repository;

import com.medichub.model.EmailVerificationToken;
import com.medichub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    /** The most recent still-usable code for a user (there should be at most one). */
    Optional<EmailVerificationToken> findFirstByUserAndUsedFalseOrderByCreatedAtDesc(User user);

    /** Burn any outstanding codes for a user before issuing a fresh one. */
    @Modifying
    @Query("update EmailVerificationToken t set t.used = true where t.user = :user and t.used = false")
    void invalidateAllForUser(@Param("user") User user);
}
