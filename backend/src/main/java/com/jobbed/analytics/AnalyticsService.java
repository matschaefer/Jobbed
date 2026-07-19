package com.jobbed.analytics;

import com.jobbed.analytics.dto.AnalyticsDtos.CompanyPerformance;
import com.jobbed.analytics.dto.AnalyticsDtos.OverviewResponse;
import com.jobbed.analytics.dto.AnalyticsDtos.SourcePerformance;
import com.jobbed.analytics.dto.AnalyticsDtos.StatusCount;
import com.jobbed.analytics.dto.AnalyticsDtos.SuccessRates;
import com.jobbed.analytics.dto.AnalyticsDtos.TimeBucket;
import com.jobbed.analytics.dto.AnalyticsDtos.TimeSeries;
import com.jobbed.application.ApplicationActivityRepository;
import com.jobbed.application.ApplicationStatus;
import com.jobbed.application.JobApplicationRepository;
import com.jobbed.interview.InterviewRepository;
import com.jobbed.reminder.ReminderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Berechnet die Dashboard-Kennzahlen strikt nutzergebunden. Die „erreichte
 * Stufe" je Bewerbung wird aus dem aktuellen Status und den STATUS_CHANGED-
 * Aktivitäten abgeleitet, damit z. B. eine später abgelehnte Bewerbung dennoch
 * als „hatte ein Interview" zählt. Berechnungsdefinitionen: siehe docs.
 */
@Service
public class AnalyticsService {

    private static final int RANK_APPLIED = 2;
    private static final int RANK_SCREENING = 3;
    private static final int RANK_INTERVIEW = 4;
    private static final int RANK_OFFER = 7;
    private static final int RANK_ACCEPTED = 8;

    private final JobApplicationRepository appRepo;
    private final ApplicationActivityRepository activityRepo;
    private final InterviewRepository interviewRepo;
    private final ReminderRepository reminderRepo;

    public AnalyticsService(JobApplicationRepository appRepo,
                            ApplicationActivityRepository activityRepo,
                            InterviewRepository interviewRepo,
                            ReminderRepository reminderRepo) {
        this.appRepo = appRepo;
        this.activityRepo = activityRepo;
        this.interviewRepo = interviewRepo;
        this.reminderRepo = reminderRepo;
    }

    @Transactional(readOnly = true)
    public OverviewResponse overview(UUID userId) {
        List<AppInsight> insights = buildInsights(userId);
        SuccessRates rates = computeRates(insights);

        long total = insights.size();
        YearMonth thisMonth = YearMonth.now();
        long thisMonthCount = insights.stream()
                .filter(a -> a.date != null && YearMonth.from(a.date).equals(thisMonth))
                .count();
        long open = insights.stream().filter(a -> isOpen(a.status)).count();
        long offers = insights.stream()
                .filter(a -> a.status == ApplicationStatus.OFFER || a.status == ApplicationStatus.ACCEPTED)
                .count();
        long rejections = insights.stream().filter(a -> a.status == ApplicationStatus.REJECTED).count();

        long upcomingInterviews = interviewRepo.countByUserIdAndStartDateTimeAfter(userId, Instant.now());
        long pendingFollowUps = reminderRepo.countByUserIdAndCompleted(userId, false);

        return new OverviewResponse(total, thisMonthCount, open, upcomingInterviews, pendingFollowUps,
                offers, rejections, rates.successRate(), rates.interviewRate());
    }

    @Transactional(readOnly = true)
    public List<StatusCount> statusDistribution(UUID userId) {
        Map<ApplicationStatus, Long> counts = new EnumMap<>(ApplicationStatus.class);
        for (AppInsight a : buildInsights(userId)) {
            counts.merge(a.status, 1L, Long::sum);
        }
        List<StatusCount> result = new ArrayList<>();
        for (ApplicationStatus status : ApplicationStatus.values()) {
            long c = counts.getOrDefault(status, 0L);
            if (c > 0) {
                result.add(new StatusCount(status, c));
            }
        }
        return result;
    }

    @Transactional(readOnly = true)
    public TimeSeries applicationsOverTime(UUID userId, String granularity) {
        boolean weekly = "week".equalsIgnoreCase(granularity);
        List<AppInsight> insights = buildInsights(userId);
        List<TimeBucket> points = weekly ? weeklyBuckets(insights, 12) : monthlyBuckets(insights, 12);
        return new TimeSeries(weekly ? "week" : "month", points);
    }

    @Transactional(readOnly = true)
    public SuccessRates successRate(UUID userId) {
        return computeRates(buildInsights(userId));
    }

    @Transactional(readOnly = true)
    public List<SourcePerformance> sourcePerformance(UUID userId) {
        Map<String, long[]> agg = new LinkedHashMap<>(); // [total, interviews, offers]
        for (AppInsight a : buildInsights(userId)) {
            String source = (a.source == null || a.source.isBlank()) ? "Unbekannt" : a.source.trim();
            long[] row = agg.computeIfAbsent(source, k -> new long[3]);
            row[0]++;
            if (a.maxRank >= RANK_INTERVIEW) row[1]++;
            if (a.maxRank >= RANK_OFFER) row[2]++;
        }
        return agg.entrySet().stream()
                .map(e -> new SourcePerformance(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2],
                        ratio(e.getValue()[2], e.getValue()[0])))
                .sorted(Comparator.comparingLong(SourcePerformance::total).reversed())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CompanyPerformance> companyPerformance(UUID userId) {
        Map<String, long[]> agg = new LinkedHashMap<>();
        for (AppInsight a : buildInsights(userId)) {
            String company = a.company == null ? "Unbekannt" : a.company;
            long[] row = agg.computeIfAbsent(company, k -> new long[3]);
            row[0]++;
            if (a.maxRank >= RANK_INTERVIEW) row[1]++;
            if (a.maxRank >= RANK_OFFER) row[2]++;
        }
        return agg.entrySet().stream()
                .map(e -> new CompanyPerformance(e.getKey(), e.getValue()[0], e.getValue()[1], e.getValue()[2]))
                .sorted(Comparator.comparingLong(CompanyPerformance::total).reversed())
                .limit(10)
                .toList();
    }

    // ---------------------------------------------------------------------

    private SuccessRates computeRates(List<AppInsight> insights) {
        long total = insights.size();
        long applied = insights.stream().filter(a -> a.maxRank >= RANK_APPLIED).count();
        long interviewed = insights.stream().filter(a -> a.maxRank >= RANK_INTERVIEW).count();
        long offered = insights.stream().filter(a -> a.maxRank >= RANK_OFFER).count();
        long accepted = insights.stream().filter(a -> a.maxRank >= RANK_ACCEPTED).count();
        long rejected = insights.stream().filter(a -> a.status == ApplicationStatus.REJECTED).count();
        long responded = insights.stream()
                .filter(a -> a.maxRank >= RANK_SCREENING || a.status == ApplicationStatus.REJECTED)
                .count();
        return new SuccessRates(total, applied, interviewed, offered, accepted, rejected,
                ratio(responded, applied), ratio(interviewed, applied),
                ratio(offered, applied), ratio(accepted, applied));
    }

    private List<AppInsight> buildInsights(UUID userId) {
        Map<UUID, Integer> maxByApp = new HashMap<>();
        for (StatusReachProjection r : activityRepo.findStatusReachesByUserId(userId)) {
            int rank = Math.max(rankOrMinus(r.getPreviousStatus()), rankOrMinus(r.getNewStatus()));
            if (rank >= 0) {
                maxByApp.merge(r.getApplicationId(), rank, Math::max);
            }
        }
        List<AppInsight> insights = new ArrayList<>();
        for (AppStatProjection s : appRepo.findStatsByUserId(userId)) {
            int max = Math.max(maxByApp.getOrDefault(s.getId(), -1), ladderRank(s.getStatus()));
            insights.add(new AppInsight(s.getStatus(), s.getApplicationDate(), s.getSource(),
                    s.getCompanyName(), max));
        }
        return insights;
    }

    private List<TimeBucket> monthlyBuckets(List<AppInsight> insights, int months) {
        Map<YearMonth, Long> counts = new HashMap<>();
        for (AppInsight a : insights) {
            if (a.date != null) {
                counts.merge(YearMonth.from(a.date), 1L, Long::sum);
            }
        }
        List<TimeBucket> buckets = new ArrayList<>();
        YearMonth cursor = YearMonth.now().minusMonths(months - 1L);
        for (int i = 0; i < months; i++) {
            buckets.add(new TimeBucket(cursor.toString(), counts.getOrDefault(cursor, 0L)));
            cursor = cursor.plusMonths(1);
        }
        return buckets;
    }

    private List<TimeBucket> weeklyBuckets(List<AppInsight> insights, int weeks) {
        Map<LocalDate, Long> counts = new HashMap<>();
        for (AppInsight a : insights) {
            if (a.date != null) {
                counts.merge(weekStart(a.date), 1L, Long::sum);
            }
        }
        List<TimeBucket> buckets = new ArrayList<>();
        LocalDate cursor = weekStart(LocalDate.now()).minusWeeks(weeks - 1L);
        DateTimeFormatter fmt = DateTimeFormatter.ISO_LOCAL_DATE;
        for (int i = 0; i < weeks; i++) {
            buckets.add(new TimeBucket(cursor.format(fmt), counts.getOrDefault(cursor, 0L)));
            cursor = cursor.plusWeeks(1);
        }
        return buckets;
    }

    private LocalDate weekStart(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1L);
    }

    private boolean isOpen(ApplicationStatus status) {
        return switch (status) {
            case ACCEPTED, REJECTED, WITHDRAWN, ARCHIVED -> false;
            default -> true;
        };
    }

    private int rankOrMinus(ApplicationStatus status) {
        return status == null ? -1 : ladderRank(status);
    }

    private int ladderRank(ApplicationStatus status) {
        return switch (status) {
            case SAVED -> 0;
            case PREPARING -> 1;
            case APPLIED -> 2;
            case SCREENING -> 3;
            case INTERVIEW -> 4;
            case TECHNICAL_INTERVIEW -> 5;
            case FINAL_INTERVIEW -> 6;
            case OFFER -> 7;
            case ACCEPTED -> 8;
            case REJECTED, WITHDRAWN, ARCHIVED -> -1;
        };
    }

    private double ratio(long numerator, long denominator) {
        if (denominator == 0) {
            return 0.0;
        }
        return Math.round((double) numerator / denominator * 10000.0) / 10000.0;
    }

    /** Interne Zwischenrepräsentation je Bewerbung. */
    private record AppInsight(ApplicationStatus status, LocalDate date, String source, String company, int maxRank) {
    }
}
