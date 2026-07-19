package com.jobbed.application;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Filterkriterien für die Bewerbungsliste (alle optional außer userId). */
public record ApplicationFilter(
        UUID userId,
        List<ApplicationStatus> statuses,
        UUID companyId,
        Priority priority,
        WorkModel workModel,
        String location,
        List<UUID> tagIds,
        LocalDate applicationDateFrom,
        LocalDate applicationDateTo,
        String query
) {
}
