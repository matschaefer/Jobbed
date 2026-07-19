package com.jobbed.notification;

import com.jobbed.common.error.exception.ResourceNotFoundException;
import com.jobbed.notification.dto.NotificationListResponse;
import com.jobbed.notification.dto.NotificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    public NotificationService(NotificationRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public NotificationListResponse list(UUID userId) {
        return new NotificationListResponse(repository.findTop50ByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::response).toList(), repository.countByUserIdAndReadFalse(userId));
    }

    @Transactional
    public void markRead(UUID userId, UUID id) {
        Notification n = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Benachrichtigung", id));
        n.setRead(true);
    }

    @Transactional
    public void markAllRead(UUID userId) {
        repository.findTop50ByUserIdOrderByCreatedAtDesc(userId).forEach(n -> n.setRead(true));
    }

    public Notification create(UUID userId, NotificationType type, String title, String message,
                               String actionUrl, String dedupeKey) {
        if (dedupeKey != null && repository.existsByUserIdAndDeduplicationKey(userId, dedupeKey)) return null;
        Notification n = new Notification();
        n.setUserId(userId); n.setNotificationType(type); n.setTitle(title); n.setMessage(message);
        n.setActionUrl(actionUrl); n.setDeduplicationKey(dedupeKey);
        return repository.save(n);
    }

    private NotificationResponse response(Notification n) {
        return new NotificationResponse(n.getId(), n.getNotificationType(), n.getTitle(), n.getMessage(),
                n.getActionUrl(), n.isRead(), n.getCreatedAt());
    }
}
