package com.jobbed.auth;

import com.jobbed.auth.token.RefreshToken;
import com.jobbed.auth.token.RefreshTokenRepository;
import com.jobbed.common.error.exception.SessionExpiredException;
import com.jobbed.common.util.TokenHasher;
import com.jobbed.security.AuthProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Verwaltet Refresh-Tokens inkl. Rotation und Wiederverwendungserkennung
 * (siehe docs/security.md). Es wird stets nur der SHA-256-Hash gespeichert.
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);

    private final RefreshTokenRepository repository;
    private final RefreshTokenReuseGuard reuseGuard;
    private final AuthProperties authProperties;

    public RefreshTokenService(RefreshTokenRepository repository,
                               RefreshTokenReuseGuard reuseGuard,
                               AuthProperties authProperties) {
        this.repository = repository;
        this.reuseGuard = reuseGuard;
        this.authProperties = authProperties;
    }

    /** Erzeugt ein neues Refresh-Token und liefert dessen Klartext (nur hier verfügbar). */
    @Transactional
    public String issue(UUID userId, String userAgent, String ipAddress) {
        String rawToken = TokenHasher.generateToken();
        RefreshToken token = new RefreshToken();
        token.setUserId(userId);
        token.setTokenHash(TokenHasher.sha256(rawToken));
        token.setExpiresAt(Instant.now().plusSeconds(authProperties.refreshTokenTtlSeconds()));
        token.setUserAgent(truncate(userAgent, 512));
        token.setIpAddress(truncate(ipAddress, 64));
        repository.save(token);
        return rawToken;
    }

    /** Rotiert ein gültiges Token; erkennt Wiederverwendung widerrufener Tokens. */
    @Transactional
    public RotationResult rotate(String rawToken, String userAgent, String ipAddress) {
        RefreshToken existing = repository.findByTokenHash(TokenHasher.sha256(rawToken))
                .orElseThrow(SessionExpiredException::new);

        if (existing.isRevoked()) {
            // Wiederverwendung eines bereits rotierten Tokens -> möglicher Diebstahl.
            log.warn("Wiederverwendung eines widerrufenen Refresh-Tokens erkannt (userId={}). "
                    + "Alle Sitzungen werden widerrufen.", existing.getUserId());
            // Widerruf in eigener Transaktion, damit er trotz nachfolgender Exception committet wird.
            reuseGuard.revokeAllForUser(existing.getUserId());
            throw new SessionExpiredException();
        }
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            throw new SessionExpiredException();
        }

        String newRaw = TokenHasher.generateToken();
        RefreshToken replacement = new RefreshToken();
        replacement.setUserId(existing.getUserId());
        replacement.setTokenHash(TokenHasher.sha256(newRaw));
        replacement.setExpiresAt(Instant.now().plusSeconds(authProperties.refreshTokenTtlSeconds()));
        replacement.setUserAgent(truncate(userAgent, 512));
        replacement.setIpAddress(truncate(ipAddress, 64));
        repository.save(replacement);

        existing.setRevoked(true);
        existing.setReplacedBy(replacement.getId());
        repository.save(existing);

        return new RotationResult(existing.getUserId(), newRaw);
    }

    /** Widerruft ein Token (Logout). Unbekannte Tokens werden still ignoriert. */
    @Transactional
    public void revoke(String rawToken) {
        repository.findByTokenHash(TokenHasher.sha256(rawToken)).ifPresent(token -> {
            token.setRevoked(true);
            repository.save(token);
        });
    }

    @Transactional
    public void revokeAllForUser(UUID userId) {
        repository.revokeAllForUser(userId);
    }

    private String truncate(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    public record RotationResult(UUID userId, String rawToken) {
    }
}
