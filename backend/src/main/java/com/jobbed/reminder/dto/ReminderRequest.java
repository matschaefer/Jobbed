package com.jobbed.reminder.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record ReminderRequest(UUID applicationId, @NotBlank @Size(max=200) String title,
        String description, @NotNull Instant reminderDateTime) {}
