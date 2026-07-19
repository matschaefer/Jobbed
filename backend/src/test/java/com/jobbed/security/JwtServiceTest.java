package com.jobbed.security;

import com.jobbed.user.Role;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET = "unit-test-jwt-secret-0123456789-abcdef";

    private JwtService jwtService(long ttl) {
        AuthProperties props = new AuthProperties(SECRET, ttl, 604800, 1440, 30,
                "http://localhost:4200",
                new AuthProperties.Cookie("refreshToken", "/api/v1/auth", false, "Strict"));
        return new JwtService(props);
    }

    @Test
    void generatesAndParsesToken() {
        JwtService service = jwtService(900);
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "a@b.de", Role.USER);

        String token = service.generateAccessToken(user);
        AuthenticatedUser parsed = service.parseAccessToken(token);

        assertThat(parsed.id()).isEqualTo(user.id());
        assertThat(parsed.email()).isEqualTo("a@b.de");
        assertThat(parsed.role()).isEqualTo(Role.USER);
    }

    @Test
    void rejectsTamperedToken() {
        JwtService service = jwtService(900);
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "a@b.de", Role.ADMIN);
        String token = service.generateAccessToken(user);

        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThatThrownBy(() -> service.parseAccessToken(tampered)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsExpiredToken() throws InterruptedException {
        JwtService service = jwtService(0); // sofort abgelaufen
        AuthenticatedUser user = new AuthenticatedUser(UUID.randomUUID(), "a@b.de", Role.USER);
        String token = service.generateAccessToken(user);
        Thread.sleep(1000);

        assertThatThrownBy(() -> service.parseAccessToken(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejectsTooShortSecret() {
        AuthProperties props = new AuthProperties("too-short", 900, 604800, 1440, 30,
                "http://localhost:4200",
                new AuthProperties.Cookie("refreshToken", "/api/v1/auth", false, "Strict"));
        assertThatThrownBy(() -> new JwtService(props)).isInstanceOf(IllegalStateException.class);
    }
}
