package com.jobbed.company.dto;

import java.util.UUID;

/** Schlanke Unternehmens-Repräsentation für Listen/eingebettete Ansichten. */
public record CompanySummaryResponse(
        UUID id,
        String name,
        String location,
        String industry,
        String logoUrl
) {
}
