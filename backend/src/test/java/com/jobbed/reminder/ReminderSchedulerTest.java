package com.jobbed.reminder;

import com.jobbed.application.JobApplicationRepository;
import com.jobbed.auth.email.EmailService;
import com.jobbed.notification.NotificationService;
import com.jobbed.user.User;
import com.jobbed.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReminderSchedulerTest {
    @Mock ReminderRepository reminders;
    @Mock UserRepository users;
    @Mock JobApplicationRepository applications;
    @Mock NotificationService notifications;
    @Mock EmailService email;

    @Test void dueReminderIsSentAndMarkedExactlyOnce() {
        UUID userId = UUID.randomUUID(); Reminder reminder = new Reminder(); reminder.setId(UUID.randomUUID());
        reminder.setUserId(userId); reminder.setReminderType(ReminderType.CUSTOM); reminder.setTitle("Nachfassen");
        User user = new User(); user.setId(userId); user.setEmail("test@jobbed.local"); user.setFirstName("Test");
        when(reminders.lockDue(any(), any())).thenReturn(List.of(reminder), List.of());
        when(users.findById(userId)).thenReturn(Optional.of(user));
        ReminderScheduler scheduler = new ReminderScheduler(reminders, users, applications, notifications, email);

        scheduler.sendDueReminders();
        scheduler.sendDueReminders();

        verify(email, times(1)).sendReminderEmail(eq("test@jobbed.local"), eq("Test"), eq("Nachfassen"), isNull(), eq("/app/calendar"));
        verify(notifications, times(1)).create(eq(userId), any(), anyString(), anyString(), anyString(), anyString());
        assertThat(reminder.isSent()).isTrue();
        assertThat(reminder.getSentAt()).isNotNull();
    }
}
