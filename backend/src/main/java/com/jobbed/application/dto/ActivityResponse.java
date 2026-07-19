package com.jobbed.application.dto;

import com.jobbed.application.ActivityType;
import com.jobbed.application.ApplicationStatus;

import java.time.Instant;
import java.util.UUID;

public record ActivityResponse(
        UUID id,
        ActivityType activityType,
        String title,
        String description,
        ApplicationStatus previousStatus,
        ApplicationStatus newStatus,
        Instant activityDate
) {
}
