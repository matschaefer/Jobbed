package com.jobbed.analytics;

import com.jobbed.analytics.dto.AnalyticsDtos.OverviewResponse;
import com.jobbed.analytics.dto.AnalyticsDtos.SourcePerformance;
import com.jobbed.analytics.dto.AnalyticsDtos.StatusCount;
import com.jobbed.analytics.dto.AnalyticsDtos.SuccessRates;
import com.jobbed.application.ApplicationActivityRepository;
import com.jobbed.application.ApplicationStatus;
import com.jobbed.application.JobApplicationRepository;
import com.jobbed.interview.InterviewRepository;
import com.jobbed.reminder.ReminderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock JobApplicationRepository appRepo;
    @Mock ApplicationActivityRepository activityRepo;
    @Mock InterviewRepository interviewRepo;
    @Mock ReminderRepository reminderRepo;

    AnalyticsService service;
    private final UUID userId = UUID.randomUUID();
    private final UUID app1 = UUID.randomUUID();
    private final UUID app2 = UUID.randomUUID();
    private final UUID app3 = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AnalyticsService(appRepo, activityRepo, interviewRepo, reminderRepo);

        lenient().when(appRepo.findStatsByUserId(userId)).thenReturn(List.of(
                stat(app1, ApplicationStatus.APPLIED, "LinkedIn", "Acme"),
                stat(app2, ApplicationStatus.REJECTED, "LinkedIn", "Acme"),
                stat(app3, ApplicationStatus.ACCEPTED, "Referral", "Globex")));

        lenient().when(activityRepo.findStatusReachesByUserId(userId)).thenReturn(List.of(
                reach(app2, ApplicationStatus.SCREENING),
                reach(app2, ApplicationStatus.INTERVIEW),
                reach(app2, ApplicationStatus.REJECTED),
                reach(app3, ApplicationStatus.OFFER),
                reach(app3, ApplicationStatus.ACCEPTED)));

        lenient().when(interviewRepo.countByUserIdAndStartDateTimeAfter(eq(userId), any())).thenReturn(5L);
        lenient().when(reminderRepo.countByUserIdAndCompleted(userId, false)).thenReturn(2L);
    }

    @Test
    void successRate_usesEverReachedStages() {
        SuccessRates r = service.successRate(userId);
        assertThat(r.total()).isEqualTo(3);
        assertThat(r.applied()).isEqualTo(3);
        assertThat(r.interviewed()).isEqualTo(2); // app2 (rejected after interview) + app3
        assertThat(r.offered()).isEqualTo(1); // app3
        assertThat(r.accepted()).isEqualTo(1); // app3
        assertThat(r.rejected()).isEqualTo(1); // app2
        assertThat(r.interviewRate()).isCloseTo(0.6667, within(0.001));
        assertThat(r.successRate()).isCloseTo(0.3333, within(0.001));
    }

    @Test
    void overview_countsOpenOffersRejections() {
        OverviewResponse o = service.overview(userId);
        assertThat(o.totalApplications()).isEqualTo(3);
        assertThat(o.openApplications()).isEqualTo(1); // only app1 (APPLIED) is open
        assertThat(o.offers()).isEqualTo(1); // app3 ACCEPTED counts as offer received
        assertThat(o.rejections()).isEqualTo(1);
        assertThat(o.upcomingInterviews()).isEqualTo(5);
        assertThat(o.pendingFollowUps()).isEqualTo(2);
        assertThat(o.interviewRate()).isCloseTo(0.6667, within(0.001));
    }

    @Test
    void statusDistribution_onlyPresentStatusesInEnumOrder() {
        List<StatusCount> dist = service.statusDistribution(userId);
        assertThat(dist).extracting(StatusCount::status)
                .containsExactly(ApplicationStatus.APPLIED, ApplicationStatus.ACCEPTED, ApplicationStatus.REJECTED);
        assertThat(dist).allMatch(s -> s.count() == 1);
    }

    @Test
    void sourcePerformance_sortedByTotalDesc() {
        List<SourcePerformance> perf = service.sourcePerformance(userId);
        assertThat(perf.get(0).source()).isEqualTo("LinkedIn");
        assertThat(perf.get(0).total()).isEqualTo(2);
        assertThat(perf.get(0).interviews()).isEqualTo(1); // app2
        assertThat(perf).anyMatch(p -> p.source().equals("Referral") && p.offers() == 1);
    }

    @Test
    void appliedThenRejected_stillCountsAsApplied_viaPreviousStatus() {
        UUID u = UUID.randomUUID();
        UUID app = UUID.randomUUID();
        when(appRepo.findStatsByUserId(u)).thenReturn(List.of(
                stat(app, ApplicationStatus.REJECTED, "X", "Y")));
        when(activityRepo.findStatusReachesByUserId(u)).thenReturn(List.of(
                reachBoth(app, ApplicationStatus.APPLIED, ApplicationStatus.REJECTED)));

        SuccessRates r = service.successRate(u);
        assertThat(r.applied()).isEqualTo(1);
        assertThat(r.rejected()).isEqualTo(1);
        assertThat(r.responseRate()).isCloseTo(1.0, within(0.001));
    }

    // --- Projection-Stubs ---

    private AppStatProjection stat(UUID id, ApplicationStatus status, String source, String company) {
        return new AppStatProjection() {
            public UUID getId() { return id; }
            public ApplicationStatus getStatus() { return status; }
            public LocalDate getApplicationDate() { return LocalDate.now(); }
            public String getSource() { return source; }
            public String getCompanyName() { return company; }
        };
    }

    private StatusReachProjection reach(UUID appId, ApplicationStatus status) {
        return reachBoth(appId, null, status);
    }

    private StatusReachProjection reachBoth(UUID appId, ApplicationStatus prev, ApplicationStatus next) {
        return new StatusReachProjection() {
            public UUID getApplicationId() { return appId; }
            public ApplicationStatus getPreviousStatus() { return prev; }
            public ApplicationStatus getNewStatus() { return next; }
        };
    }
}
