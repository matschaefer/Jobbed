package com.jobbed.reminder;

import com.jobbed.interview.Interview;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reminder")
@Getter @Setter @NoArgsConstructor
public class Reminder {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Column(name = "application_id") private UUID applicationId;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "interview_id") private Interview interview;
    @Enumerated(EnumType.STRING) @Column(name = "reminder_type", nullable = false, length = 20)
    private ReminderType reminderType = ReminderType.CUSTOM;
    @Column(nullable = false, length = 200) private String title;
    @Column(columnDefinition = "text") private String description;
    @Column(name = "reminder_date_time", nullable = false) private Instant reminderDateTime;
    @Column(nullable = false) private boolean completed;
    @Column(nullable = false) private boolean sent;
    @Column(name = "sent_at") private Instant sentAt;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @UpdateTimestamp @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
