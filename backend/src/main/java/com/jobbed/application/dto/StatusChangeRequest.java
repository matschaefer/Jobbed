package com.jobbed.application.dto;

import com.jobbed.application.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record StatusChangeRequest(
        @NotNull(message = "Ein Zielstatus ist erforderlich.")
        ApplicationStatus newStatus,
        String note
) {
}
