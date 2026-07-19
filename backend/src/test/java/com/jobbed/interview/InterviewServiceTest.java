package com.jobbed.interview;

import com.jobbed.application.*;
import com.jobbed.company.Company;
import com.jobbed.interview.dto.InterviewRequest;
import com.jobbed.reminder.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InterviewServiceTest {
    @Mock InterviewRepository interviews;
    @Mock JobApplicationRepository applications;
    @Mock ReminderRepository reminders;
    @Mock ApplicationActivityRepository activities;
    InterviewService service;

    @BeforeEach void setUp() { service = new InterviewService(interviews, applications, reminders, activities); }

    @Test void create_buildsReminderAtConfiguredOffsetAndActivity() {
        UUID userId = UUID.randomUUID(); UUID appId = UUID.randomUUID(); UUID interviewId = UUID.randomUUID();
        Company company = new Company(); company.setId(UUID.randomUUID()); company.setName("Acme");
        JobApplication app = new JobApplication(); app.setId(appId); app.setUserId(userId); app.setCompany(company); app.setJobTitle("Developer");
        when(applications.findByIdAndUserId(appId, userId)).thenReturn(Optional.of(app));
        when(interviews.save(any())).thenAnswer(invocation -> { Interview i = invocation.getArgument(0); i.setId(interviewId); return i; });
        when(reminders.findByInterview_IdAndReminderType(interviewId, ReminderType.INTERVIEW)).thenReturn(Optional.empty());
        Instant start = Instant.parse("2026-08-01T10:00:00Z");
        var request = new InterviewRequest(appId, InterviewType.VIDEO, "Tech Talk", start,
                start.plusSeconds(3600), "Europe/Berlin", null, null, null, null,
                InterviewResult.PENDING, true, 90);

        service.create(userId, request);

        ArgumentCaptor<Reminder> reminder = ArgumentCaptor.forClass(Reminder.class);
        verify(reminders).save(reminder.capture());
        assertThat(reminder.getValue().getReminderDateTime()).isEqualTo(start.minusSeconds(5400));
        assertThat(reminder.getValue().getReminderType()).isEqualTo(ReminderType.INTERVIEW);
        verify(activities).save(argThat(a -> a.getActivityType() == ActivityType.INTERVIEW_SCHEDULED));
    }
}
