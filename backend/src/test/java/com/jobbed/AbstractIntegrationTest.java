package com.jobbed;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Basisklasse für Integrationstests. Startet eine echte PostgreSQL-Instanz via
 * Testcontainers; Spring bindet die Datasource automatisch (@ServiceConnection).
 * Voraussetzung: laufender Docker-Daemon.
 */
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    protected static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        // Ein Container pro Test-JVM: Spring darf den Context zwischen IT-Klassen
        // cachen, ohne dass der darin gebundene dynamische Port ungültig wird.
        POSTGRES.start();
    }
}
