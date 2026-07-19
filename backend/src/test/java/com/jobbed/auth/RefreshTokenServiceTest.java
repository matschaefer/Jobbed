package com.jobbed.auth;

import com.jobbed.auth.token.RefreshToken;
import com.jobbed.auth.token.RefreshTokenRepository;
import com.jobbed.common.error.exception.SessionExpiredException;
import com.jobbed.common.util.TokenHasher;
import com.jobbed.security.AuthProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock RefreshTokenRepository repository;
    @Mock RefreshTokenReuseGuard reuseGuard;

    RefreshTokenService service;

    private final AuthProperties props = new AuthProperties("test-secret-1234567890-abcdefghijklmnop",
            900, 604800, 1440, 30, "http://localhost:4200",
            new AuthProperties.Cookie("refreshToken", "/api/v1/auth", false, "Strict"));

    @BeforeEach
    void setUp() {
        service = new RefreshTokenService(repository, reuseGuard, props);
    }

    @Test
    void rotate_unknownToken_throwsSessionExpired() {
        when(repository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.rotate("raw", "UA", "ip"))
                .isInstanceOf(SessionExpiredException.class);
        verify(reuseGuard, never()).revokeAllForUser(any());
    }

    @Test
    void rotate_revokedToken_triggersReuseGuardAndThrows() {
        UUID userId = UUID.randomUUID();
        RefreshToken revoked = new RefreshToken();
        revoked.setUserId(userId);
        revoked.setRevoked(true);
        revoked.setExpiresAt(Instant.now().plusSeconds(3600));
        when(repository.findByTokenHash(TokenHasher.sha256("raw"))).thenReturn(Optional.of(revoked));

        assertThatThrownBy(() -> service.rotate("raw", "UA", "ip"))
                .isInstanceOf(SessionExpiredException.class);
        verify(reuseGuard).revokeAllForUser(userId);
    }

    @Test
    void rotate_validToken_rotatesAndRevokesOld() {
        UUID userId = UUID.randomUUID();
        RefreshToken active = new RefreshToken();
        active.setId(UUID.randomUUID());
        active.setUserId(userId);
        active.setRevoked(false);
        active.setExpiresAt(Instant.now().plusSeconds(3600));
        when(repository.findByTokenHash(TokenHasher.sha256("raw"))).thenReturn(Optional.of(active));

        RefreshTokenService.RotationResult result = service.rotate("raw", "UA", "ip");

        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.rawToken()).isNotBlank();
        assertThat(active.isRevoked()).isTrue();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository, org.mockito.Mockito.atLeast(2)).save(captor.capture());
        // Ein neues, aktives Ersatz-Token wurde gespeichert.
        assertThat(captor.getAllValues()).anyMatch(t -> !t.isRevoked());
    }
}
