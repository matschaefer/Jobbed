package com.jobbed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Einstiegspunkt der Jobbed-Backend-Anwendung.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class JobbedApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobbedApplication.class, args);
    }
}
