package com.jobbed.analytics.dto;

import com.jobbed.application.ApplicationStatus;

import java.util.List;

/**
 * Sammelbehälter für die Analytics-Antworttypen (records).
 * Alle Kennzahlen sind strikt nutzergebunden berechnet.
 */
public final class AnalyticsDtos {

    private AnalyticsDtos() {
    }

    /** Aggregat-Kennzahlen fürs Dashboard. */
    public record OverviewResponse(
            long totalApplications,
            long applicationsThisMonth,
            long openApplications,
            long upcomingInterviews,
            long pendingFollowUps,
            long offers,
            long rejections,
            double successRate,
            double interviewRate
    ) {
    }

    public record StatusCount(ApplicationStatus status, long count) {
    }

    /** Zeitreihenpunkt (period z. B. "2026-07" oder ISO-Wochenstart). */
    public record TimeBucket(String period, long count) {
    }

    /**
     * Trichter-/Quotenkennzahlen. Basis (Nenner) ist die Zahl der tatsächlich
     * eingereichten Bewerbungen (mind. Status APPLIED je erreicht).
     */
    public record SuccessRates(
            long total,
            long applied,
            long interviewed,
            long offered,
            long accepted,
            long rejected,
            double responseRate,
            double interviewRate,
            double offerRate,
            double successRate
    ) {
    }

    public record SourcePerformance(
            String source,
            long total,
            long interviews,
            long offers,
            double offerRate
    ) {
    }

    public record CompanyPerformance(
            String company,
            long total,
            long interviews,
            long offers
    ) {
    }

    /** Wrapper, damit die Zeitreihe die gewählte Granularität mitteilt. */
    public record TimeSeries(String granularity, List<TimeBucket> points) {
    }
}
