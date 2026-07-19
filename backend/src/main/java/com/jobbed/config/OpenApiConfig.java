package com.jobbed.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI-/Swagger-Konfiguration. Das Bearer-Security-Schema wird bereits hier
 * definiert, damit geschützte Endpunkte korrekt dokumentiert
 * sind.
 */
@Configuration
public class OpenApiConfig {

    private static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI jobbedOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Jobbed API")
                        .description("REST-API des Bewerbungs-Trackers Jobbed")
                        .version("v1")
                        .contact(new Contact().name("Jobbed").email("no-reply@jobbed.local"))
                        .license(new License().name("MIT")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT-Access-Token im Authorization-Header")));
    }
}
