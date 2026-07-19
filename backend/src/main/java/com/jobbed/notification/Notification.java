package com.jobbed.notification;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "notification")
@Getter @Setter @NoArgsConstructor
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "user_id", nullable = false) private UUID userId;
    @Enumerated(EnumType.STRING) @Column(name = "notification_type", nullable = false, length = 30)
    private NotificationType notificationType;
    @Column(nullable = false, length = 200) private String title;
    @Column(nullable = false, columnDefinition = "text") private String message;
    @Column(name = "action_url", length = 500) private String actionUrl;
    @Column(name = "deduplication_key", length = 255) private String deduplicationKey;
    @Column(nullable = false) private boolean read;
    @CreationTimestamp @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
