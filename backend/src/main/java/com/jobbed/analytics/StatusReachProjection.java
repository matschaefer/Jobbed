package com.jobbed.analytics;

import com.jobbed.application.ApplicationStatus;

import java.util.UUID;

/**
 * Projektion, welche Status eine Bewerbung laut Aktivitäten je berührt hat.
 * Sowohl {@code previousStatus} als auch {@code newStatus} zählen als „erreicht",
 * damit der ursprüngliche Status auch bei späterem Wechsel (z. B. → REJECTED)
 * nicht verloren geht.
 */
public interface StatusReachProjection {
    UUID getApplicationId();

    ApplicationStatus getPreviousStatus();

    ApplicationStatus getNewStatus();
}
