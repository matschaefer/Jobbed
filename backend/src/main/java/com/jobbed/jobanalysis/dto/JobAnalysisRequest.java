package com.jobbed.jobanalysis.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;

public record JobAnalysisRequest(
        @NotBlank @Size(max = 50_000) String jobDescription,
        @Size(max = 100) List<@Size(max = 80) String> profileSkills
) {}
