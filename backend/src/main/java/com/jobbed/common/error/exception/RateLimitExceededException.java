package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/** Zu viele Versuche (Login/Refresh/Passwort-vergessen). HTTP 429. */
public class RateLimitExceededException extends ApiException {

    private final long retryAfterSeconds;

    public RateLimitExceededException(long retryAfterSeconds) {
        super(ErrorCode.RATE_LIMIT_EXCEEDED,
                "Zu viele Versuche. Bitte versuche es später erneut.");
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public long getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
