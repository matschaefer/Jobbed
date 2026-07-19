package com.jobbed.application;

/** Statuswerte einer Bewerbung (siehe docs/data-model.md, Status-Workflow). */
public enum ApplicationStatus {
    SAVED,
    PREPARING,
    APPLIED,
    SCREENING,
    INTERVIEW,
    TECHNICAL_INTERVIEW,
    FINAL_INTERVIEW,
    OFFER,
    ACCEPTED,
    REJECTED,
    WITHDRAWN,
    ARCHIVED
}
