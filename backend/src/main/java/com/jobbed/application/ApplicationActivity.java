package com.jobbed.application;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/** Eintrag in der Aktivitäten-Timeline einer Bewerbung. */
@Entity
@Table(name = "application_activity", indexes = {
        @Index(name = "idx_activity_application_date", columnList = "application_id, activity_date")
})
@Getter
@Setter
@NoArgsConstructor
public class ApplicationActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false, length = 30)
    private ActivityType activityType;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "previous_status", length = 30)
    private ApplicationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 30)
    private ApplicationStatus newStatus;

    @Column(name = "activity_date", nullable = false)
    private Instant activityDate;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
