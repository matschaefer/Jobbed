package com.jobbed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Konfigurierbare CORS-Einstellungen (Quelle: application.yml / Umgebung).
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
        List<String> allowedOrigins
) {
    public CorsProperties {
        if (allowedOrigins == null) {
            allowedOrigins = List.of("http://localhost:4200");
        }
    }
}
