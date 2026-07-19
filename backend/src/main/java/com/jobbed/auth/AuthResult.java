package com.jobbed.auth;

import com.jobbed.auth.dto.AuthResponse;

/**
 * Ergebnis von Login/Refresh: die an den Client gehende {@link AuthResponse}
 * plus das Klartext-Refresh-Token, das der Controller als Cookie setzt.
 */
public record AuthResult(AuthResponse response, String rawRefreshToken) {
}
