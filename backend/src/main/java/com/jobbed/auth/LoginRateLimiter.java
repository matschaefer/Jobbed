package com.jobbed.auth;

import com.jobbed.common.error.exception.RateLimitExceededException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Einfaches, prozesslokales Rate-Limiting gegen Brute-Force auf sicherheits-
 * kritischen Endpunkten (Login, Passwort-vergessen). Nach {@code MAX_ATTEMPTS}
 * Fehlversuchen innerhalb des Fensters wird der Schlüssel temporär gesperrt.
 *
 * <p>Bewusst in-memory gehalten; für horizontale Skalierung müsste ein
 * verteilter Speicher (z. B. Redis/Bucket4j) ergänzt werden – siehe docs/risks.md.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private static final Duration LOCKOUT = Duration.ofMinutes(15);

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    /** Wirft {@link RateLimitExceededException}, wenn der Schlüssel gesperrt ist. */
    public void checkAllowed(String key) {
        Attempt attempt = attempts.get(key);
        if (attempt != null && attempt.lockedUntil != null) {
            long retryAfter = Duration.between(Instant.now(), attempt.lockedUntil).getSeconds();
            if (retryAfter > 0) {
                throw new RateLimitExceededException(retryAfter);
            }
        }
    }

    public void recordFailure(String key) {
        Instant now = Instant.now();
        attempts.compute(key, (k, existing) -> {
            Attempt a = (existing == null || existing.isWindowExpired(now)) ? new Attempt(now) : existing;
            a.count++;
            if (a.count >= MAX_ATTEMPTS) {
                a.lockedUntil = now.plus(LOCKOUT);
            }
            return a;
        });
    }

    public void recordSuccess(String key) {
        attempts.remove(key);
    }

    private static final class Attempt {
        private final Instant windowStart;
        private int count;
        private Instant lockedUntil;

        private Attempt(Instant windowStart) {
            this.windowStart = windowStart;
        }

        private boolean isWindowExpired(Instant now) {
            return lockedUntil == null && windowStart.plus(WINDOW).isBefore(now);
        }
    }
}
