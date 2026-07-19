package com.jobbed.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.demo-mode")
public record DemoModeProperties(
        boolean enabled,
        String email
) {
    public DemoModeProperties {
        if (email == null || email.isBlank()) {
            email = "demo@jobbed.local";
        }
    }
}
