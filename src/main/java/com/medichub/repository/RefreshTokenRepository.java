package com.medichub.repository;

import com.medichub.model.RefreshToken;
import com.medichub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /** Lookup by the unique (indexed) token column. */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Revoke every live token for a user in a single statement — used on password
     * reset and "log out everywhere". A bulk update avoids loading N rows.
     */
    @Modifying
    @Query("update RefreshToken rt set rt.revoked = true where rt.user = :user and rt.revoked = false")
    int revokeAllForUser(@Param("user") User user);
}
