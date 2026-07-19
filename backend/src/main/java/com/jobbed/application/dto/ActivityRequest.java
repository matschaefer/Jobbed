package com.jobbed.application.dto;

import com.jobbed.application.ActivityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ActivityRequest(
        ActivityType activityType,

        @NotBlank(message = "Ein Titel ist erforderlich.")
        @Size(max = 200)
        String title,

        String description
) {
}
