package com.jobbed.auth.token;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

/**
 * Persistiertes Refresh-Token. Gespeichert wird nur der SHA-256-Hash des opaken
 * Tokens. Rotation: bei Nutzung wird das alte Token widerrufen und über
 * {@code replacedBy} mit dem Nachfolger verkettet (siehe docs/security.md).
 */
@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "idx_refresh_token_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    private boolean revoked = false;

    @Column(name = "replaced_by")
    private UUID replacedBy;

    @Column(name = "user_agent", length = 512)
    private String userAgent;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public boolean isActive() {
        return !revoked && expiresAt.isAfter(Instant.now());
    }
}
