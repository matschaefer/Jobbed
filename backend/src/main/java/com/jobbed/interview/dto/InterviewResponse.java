package com.jobbed.interview.dto;

import com.jobbed.interview.InterviewResult;
import com.jobbed.interview.InterviewType;
import java.time.Instant;
import java.util.UUID;

public record InterviewResponse(UUID id, UUID applicationId, String applicationTitle, String companyName,
        InterviewType interviewType, String title, Instant startDateTime, Instant endDateTime,
        String timeZone, String location, String meetingUrl, String interviewerNames, String notes,
        InterviewResult result, boolean reminderEnabled, int reminderMinutesBefore,
        Instant createdAt, Instant updatedAt) {}
