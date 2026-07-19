package com.jobbed.company.dto;

import java.time.Instant;
import java.util.UUID;

/** Vollständige Unternehmens-Detailansicht. */
public record CompanyResponse(
        UUID id,
        String name,
        String website,
        String industry,
        String companySize,
        String location,
        String description,
        String logoUrl,
        long applicationCount,
        Instant createdAt,
        Instant updatedAt
) {
}
