package com.jobbed.resume.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResumeGenerationRequest(
        @NotBlank @Size(max = 160) String fullName,
        @NotBlank @Email @Size(max = 254) String email,
        @Size(max = 80) String phone,
        @Size(max = 160) String location,
        @Size(max = 200) String headline,
        @Size(max = 3_000) String professionalSummary,
        @Size(max = 5_000) String skills,
        @Size(max = 15_000) String experience,
        @Size(max = 8_000) String education,
        @Size(max = 2_000) String languages,
        @Size(max = 200) String targetRole,
        @Size(max = 20_000) String jobDescription
) {}
