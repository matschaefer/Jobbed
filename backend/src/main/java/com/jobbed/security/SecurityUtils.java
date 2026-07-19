package com.jobbed.security;

import com.jobbed.common.error.exception.SessionExpiredException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

/**
 * Zugriff auf den aktuell authentifizierten Nutzer – die einzige zulässige
 * Quelle für die {@code userId} (nie aus dem Request).
 */
public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<AuthenticatedUser> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser user) {
            return Optional.of(user);
        }
        return Optional.empty();
    }

    public static UUID currentUserId() {
        return currentUser().map(AuthenticatedUser::id).orElseThrow(SessionExpiredException::new);
    }
}
