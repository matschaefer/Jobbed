package com.jobbed.notification.dto;

import com.jobbed.notification.NotificationType;
import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(UUID id, NotificationType notificationType, String title,
        String message, String actionUrl, boolean read, Instant createdAt) {}
