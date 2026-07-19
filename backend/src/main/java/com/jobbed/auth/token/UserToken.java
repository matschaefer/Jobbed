package com.jobbed.auth.token;

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

/**
 * Einmal-Token für E-Mail-Verifikation und Passwort-Reset. Gespeichert wird nur
 * der SHA-256-Hash; nach Verwendung wird {@code usedAt} gesetzt.
 */
@Entity
@Table(name = "user_token", indexes = {
        @Index(name = "idx_user_token_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
public class UserToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UserTokenType type;

    @Column(name = "token_hash", nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "used_at")
    private Instant usedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public boolean isUsable() {
        return usedAt == null && expiresAt.isAfter(Instant.now());
    }
}
