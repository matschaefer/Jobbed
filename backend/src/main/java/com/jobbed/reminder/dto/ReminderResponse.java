package com.jobbed.reminder.dto;

import com.jobbed.reminder.ReminderType;
import java.time.Instant;
import java.util.UUID;

public record ReminderResponse(UUID id, UUID applicationId, UUID interviewId, ReminderType reminderType,
        String title, String description, Instant reminderDateTime, boolean completed, boolean sent, Instant sentAt) {}
