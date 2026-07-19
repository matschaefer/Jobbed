package com.jobbed.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Zentrale Auth-Konfiguration (Quelle: application.yml / Umgebung, Präfix
 * {@code app.auth}). Das JWT-Secret muss aus der Umgebung stammen.
 */
@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(
        String jwtSecret,
        @DefaultValue("900") long accessTokenTtlSeconds,
        @DefaultValue("604800") long refreshTokenTtlSeconds,
        @DefaultValue("1440") long verificationTokenTtlMinutes,
        @DefaultValue("30") long resetTokenTtlMinutes,
        @DefaultValue("http://localhost:4200") String frontendBaseUrl,
        @DefaultValue Cookie cookie
) {
    public record Cookie(
            @DefaultValue("refreshToken") String name,
            @DefaultValue("/api/v1/auth") String path,
            @DefaultValue("false") boolean secure,
            @DefaultValue("Strict") String sameSite
    ) {
    }
}
