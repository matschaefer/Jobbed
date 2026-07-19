package com.jobbed.auth;

import com.jobbed.common.error.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginRateLimiterTest {

    @Test
    void allowsUpToLimitThenBlocks() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        String key = "user@b.de|127.0.0.1";

        for (int i = 0; i < 5; i++) {
            assertThatCode(() -> limiter.checkAllowed(key)).doesNotThrowAnyException();
            limiter.recordFailure(key);
        }

        assertThatThrownBy(() -> limiter.checkAllowed(key))
                .isInstanceOf(RateLimitExceededException.class);
    }

    @Test
    void successResetsCounter() {
        LoginRateLimiter limiter = new LoginRateLimiter();
        String key = "user@b.de|127.0.0.1";

        for (int i = 0; i < 4; i++) {
            limiter.recordFailure(key);
        }
        limiter.recordSuccess(key);

        assertThatCode(() -> limiter.checkAllowed(key)).doesNotThrowAnyException();
    }
}
