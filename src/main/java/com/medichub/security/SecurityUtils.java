package com.medichub.security;

import com.medichub.exception.AccessDeniedException;
import com.medichub.model.enums.Role;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Convenience accessors for the currently authenticated principal. The JWT filter
 * stores a {@link CustomUserDetails} (carrying the user id + role from the token),
 * so these read straight from the security context with no DB hit.
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static CustomUserDetails currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof CustomUserDetails principal)) {
            throw new AccessDeniedException("No authenticated user in context");
        }
        return principal;
    }

    public static Long currentUserId() {
        return currentUser().getId();
    }

    public static Role currentRole() {
        return currentUser().getRole();
    }

    public static boolean isAdmin() {
        return currentRole() == Role.ADMIN;
    }
}
