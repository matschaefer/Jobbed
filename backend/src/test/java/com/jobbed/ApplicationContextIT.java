package com.jobbed;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Stellt sicher, dass der gesamte Anwendungskontext startet und die
 * Flyway-Migrationen erfolgreich gegen PostgreSQL laufen.
 */
@SpringBootTest
class ApplicationContextIT extends AbstractIntegrationTest {

    @Autowired
    ApplicationContext context;

    @Test
    void contextLoads() {
        assertThat(context).isNotNull();
        assertThat(context.getBeanDefinitionCount()).isPositive();
    }
}
