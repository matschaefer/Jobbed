package com.jobbed.common.error;

import org.springframework.http.HttpStatus;

/**
 * Stabile, maschinenlesbare Fehlercodes des einheitlichen Fehlerformats.
 * Jeder Code ist genau einem HTTP-Status zugeordnet.
 */
public enum ErrorCode {

    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    MALFORMED_REQUEST(HttpStatus.BAD_REQUEST),
    AUTHENTICATION_REQUIRED(HttpStatus.UNAUTHORIZED),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    INVALID_TOKEN(HttpStatus.BAD_REQUEST),
    ACCESS_DENIED(HttpStatus.FORBIDDEN),
    EMAIL_NOT_VERIFIED(HttpStatus.FORBIDDEN),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE),
    UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    BUSINESS_RULE_VIOLATION(HttpStatus.UNPROCESSABLE_ENTITY),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS),
    EMAIL_DELIVERY_FAILED(HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus status;

    ErrorCode(HttpStatus status) {
        this.status = status;
    }

    public HttpStatus status() {
        return status;
    }
}
