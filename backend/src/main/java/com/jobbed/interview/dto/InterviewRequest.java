package com.jobbed.interview.dto;

import com.jobbed.interview.InterviewResult;
import com.jobbed.interview.InterviewType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record InterviewRequest(
        @NotNull UUID applicationId,
        @NotNull InterviewType interviewType,
        @NotBlank @Size(max = 200) String title,
        @NotNull Instant startDateTime,
        @NotNull Instant endDateTime,
        @NotBlank @Size(max = 80) String timeZone,
        @Size(max = 255) String location,
        @Size(max = 500) String meetingUrl,
        @Size(max = 500) String interviewerNames,
        String notes,
        InterviewResult result,
        boolean reminderEnabled,
        @Min(0) Integer reminderMinutesBefore) {}
