package com.jobbed.application.dto;

import com.jobbed.application.ApplicationStatus;
import com.jobbed.application.EmploymentType;
import com.jobbed.application.Priority;
import com.jobbed.application.WorkModel;
import com.jobbed.company.dto.CompanySummaryResponse;
import com.jobbed.contact.dto.ContactResponse;
import com.jobbed.tag.dto.TagResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Vollständige Detailansicht einer Bewerbung. */
public record ApplicationDetailResponse(
        UUID id,
        CompanySummaryResponse company,
        ContactResponse contactPerson,
        String jobTitle,
        String jobDescription,
        String source,
        String jobUrl,
        EmploymentType employmentType,
        WorkModel workModel,
        String location,
        BigDecimal salaryMin,
        BigDecimal salaryMax,
        String currency,
        LocalDate applicationDate,
        ApplicationStatus currentStatus,
        Priority priority,
        Short rating,
        LocalDate deadline,
        LocalDate nextActionDate,
        String notes,
        String rejectionReason,
        List<TagResponse> tags,
        Instant createdAt,
        Instant updatedAt
) {
}
