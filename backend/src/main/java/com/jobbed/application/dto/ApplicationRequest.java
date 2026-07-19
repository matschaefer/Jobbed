package com.jobbed.application.dto;

import com.jobbed.application.ApplicationStatus;
import com.jobbed.application.EmploymentType;
import com.jobbed.application.Priority;
import com.jobbed.application.WorkModel;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

/**
 * Anlage/Aktualisierung einer Bewerbung. Bei {@code PATCH} werden nur die
 * gesetzten (nicht-null) Felder übernommen.
 */
public record ApplicationRequest(
        @NotNull(message = "Ein Unternehmen ist erforderlich.")
        UUID companyId,

        UUID contactPersonId,

        @NotBlank(message = "Die Stellenbezeichnung darf nicht leer sein.")
        @Size(max = 200)
        String jobTitle,

        String jobDescription,
        @Size(max = 120) String source,
        @Size(max = 500) String jobUrl,
        EmploymentType employmentType,
        WorkModel workModel,
        @Size(max = 200) String location,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        @Size(max = 3) String currency,
        LocalDate applicationDate,
        ApplicationStatus currentStatus,
        Priority priority,
        @Min(1) @Max(5) Short rating,
        LocalDate deadline,
        LocalDate nextActionDate,
        String notes,
        String rejectionReason,
        Set<UUID> tagIds
) {
}
