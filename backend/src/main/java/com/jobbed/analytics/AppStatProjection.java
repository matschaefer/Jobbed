package com.jobbed.analytics;

import com.jobbed.application.ApplicationStatus;

import java.time.LocalDate;
import java.util.UUID;

/** Schlanke Projektion einer Bewerbung für Auswertungen (kein Entity-Load). */
public interface AppStatProjection {
    UUID getId();

    ApplicationStatus getStatus();

    LocalDate getApplicationDate();

    String getSource();

    String getCompanyName();
}
