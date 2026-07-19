package com.jobbed.security;

import com.jobbed.user.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Erzeugt und verifiziert JWT-Access-Tokens (HS256). Refresh-Tokens sind opak
 * und werden hier bewusst nicht behandelt.
 */
@Service
public class JwtService {

    private static final String CLAIM_EMAIL = "email";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long accessTokenTtlSeconds;

    public JwtService(AuthProperties properties) {
        byte[] secretBytes = properties.jwtSecret().getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < 32) {
            throw new IllegalStateException(
                    "app.auth.jwt-secret muss mindestens 32 Byte lang sein (HS256).");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.accessTokenTtlSeconds = properties.accessTokenTtlSeconds();
    }

    public String generateAccessToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(accessTokenTtlSeconds);
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.id().toString())
                .claim(CLAIM_EMAIL, user.email())
                .claim(CLAIM_ROLE, user.role().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(key)
                .compact();
    }

    /**
     * Verifiziert das Token und liefert den Principal. Wirft {@link JwtException}
     * bei ungültiger Signatur oder abgelaufenem Token.
     */
    public AuthenticatedUser parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return new AuthenticatedUser(
                UUID.fromString(claims.getSubject()),
                claims.get(CLAIM_EMAIL, String.class),
                Role.valueOf(claims.get(CLAIM_ROLE, String.class)));
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }
}
