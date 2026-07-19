package com.jobbed.meta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Öffentlicher Meta-Endpunkt für einen einfachen Erreichbarkeits-Check der API.
 * Fachliche Endpunkte folgen in den späteren Phasen.
 */
@RestController
@RequestMapping("/api/v1/meta")
@Tag(name = "Meta", description = "Allgemeine Informationen zur API")
public class MetaController {

    private final String appName;
    private final String appVersion;

    public MetaController(@Value("${spring.application.name:jobbed}") String appName,
                          @Value("${app.version:0.1.0}") String appVersion) {
        this.appName = appName;
        this.appVersion = appVersion;
    }

    @GetMapping("/ping")
    @Operation(summary = "Erreichbarkeit prüfen",
            description = "Liefert Name, Version und Zeitstempel der API.")
    public Map<String, Object> ping() {
        return Map.of(
                "application", appName,
                "version", appVersion,
                "status", "UP",
                "timestamp", Instant.now().toString()
        );
    }
}
