package com.jobbed.contact.dto;

import com.jobbed.company.dto.CompanySummaryResponse;

import java.time.Instant;
import java.util.UUID;

public record ContactResponse(
        UUID id,
        CompanySummaryResponse company,
        String firstName,
        String lastName,
        String position,
        String email,
        String phone,
        String linkedInUrl,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
