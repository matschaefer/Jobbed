package com.jobbed.interview;

import com.jobbed.application.JobApplication;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "interview")
@Getter @Setter @NoArgsConstructor
public class Interview {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false) private JobApplication application;
    @Enumerated(EnumType.STRING) @Column(name = "interview_type", nullable = false, length = 30)
    private InterviewType interviewType;
    @Column(nullable = false, length = 200) private String title;
    @Column(name = "start_date_time", nullable = false) private Instant startDateTime;
    @Column(name = "end_date_time", nullable = false) private Instant endDateTime;
    @Column(name = "time_zone", nullable = false, length = 80) private String timeZone;
    @Column(length = 255) private String location;
    @Column(name = "meeting_url", length = 500) private String meetingUrl;
    @Column(name = "interviewer_names", length = 500) private String interviewerNames;
    @Column(columnDefinition = "text") private String notes;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private InterviewResult result = InterviewResult.PENDING;
    @Column(name = "reminder_enabled", nullable = false) private boolean reminderEnabled;
    @Column(name = "reminder_minutes_before", nullable = false) private int reminderMinutesBefore = 60;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
