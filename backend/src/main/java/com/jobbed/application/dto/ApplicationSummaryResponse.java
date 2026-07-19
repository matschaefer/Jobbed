package com.jobbed.application.dto;

import com.jobbed.application.ApplicationStatus;
import com.jobbed.application.Priority;
import com.jobbed.application.WorkModel;
import com.jobbed.company.dto.CompanySummaryResponse;
import com.jobbed.tag.dto.TagResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/** Listen-/Kanban-Repräsentation einer Bewerbung. */
public record ApplicationSummaryResponse(
        UUID id,
        String jobTitle,
        CompanySummaryResponse company,
        String location,
        WorkModel workModel,
        ApplicationStatus currentStatus,
        Priority priority,
        Short rating,
        LocalDate applicationDate,
        LocalDate nextActionDate,
        LocalDate deadline,
        List<TagResponse> tags
) {
}
