package com.medichub.repository;

import com.medichub.model.PasswordResetToken;
import com.medichub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /** Lookup by the unique (indexed) token column. */
    Optional<PasswordResetToken> findByToken(String token);

    /** Invalidate any outstanding reset tokens for a user before issuing a fresh one. */
    @Modifying
    @Query("update PasswordResetToken t set t.used = true where t.user = :user and t.used = false")
    int invalidateAllForUser(@Param("user") User user);
}
