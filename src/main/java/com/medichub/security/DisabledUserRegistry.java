package com.medichub.security;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory set of currently-disabled user ids, consulted by {@link JwtAuthenticationFilter}
 * so a disabled account is rejected immediately rather than remaining valid until its
 * access token expires. Populated by the admin enable/disable action and seeded from the
 * database on startup (so it survives restarts).
 *
 * <p>Per-instance state — correct for the single-instance MVP. A multi-instance deployment
 * would back this with a shared store (e.g. Redis).
 */
@Component
public class DisabledUserRegistry {

    private final Set<Long> disabledUserIds = ConcurrentHashMap.newKeySet();

    public void markDisabled(Long userId) {
        disabledUserIds.add(userId);
    }

    public void markEnabled(Long userId) {
        disabledUserIds.remove(userId);
    }

    public boolean isDisabled(Long userId) {
        return userId != null && disabledUserIds.contains(userId);
    }
}
