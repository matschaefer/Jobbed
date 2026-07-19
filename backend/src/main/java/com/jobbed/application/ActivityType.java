package com.jobbed.application;

/** Typ eines Aktivitäts-/Timeline-Eintrags einer Bewerbung. */
public enum ActivityType {
    CREATED,
    STATUS_CHANGED,
    NOTE_ADDED,
    EMAIL_SENT,
    INTERVIEW_SCHEDULED,
    FOLLOW_UP,
    DOCUMENT_UPLOADED,
    OFFER_RECEIVED,
    REJECTED,
    CUSTOM
}
