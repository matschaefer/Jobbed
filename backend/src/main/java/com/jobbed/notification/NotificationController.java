package com.jobbed.notification;

import com.jobbed.notification.dto.NotificationListResponse;
import com.jobbed.security.SecurityUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }
    @GetMapping public NotificationListResponse list() { return service.list(SecurityUtils.currentUserId()); }
    @PatchMapping("/{id}/read") public ResponseEntity<Void> read(@PathVariable UUID id) {
        service.markRead(SecurityUtils.currentUserId(), id); return ResponseEntity.noContent().build();
    }
    @PatchMapping("/read-all") public ResponseEntity<Void> readAll() {
        service.markAllRead(SecurityUtils.currentUserId()); return ResponseEntity.noContent().build();
    }
}
