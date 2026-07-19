package com.jobbed.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.Instant;
import java.util.List;

/**
 * Einheitliches Fehlerformat aller API-Antworten (siehe docs/api-design.md).
 * {@code fieldErrors} wird nur bei Validierungsfehlern gesetzt.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        String correlationId,
        List<ApiFieldError> fieldErrors
) {
    public static ApiError of(ErrorCode code, String message, String path, String correlationId,
                              List<ApiFieldError> fieldErrors) {
        return new ApiError(
                Instant.now(),
                code.status().value(),
                code.status().getReasonPhrase(),
                code.name(),
                message,
                path,
                correlationId,
                fieldErrors == null || fieldErrors.isEmpty() ? null : fieldErrors
        );
    }

    public static ApiError of(ErrorCode code, String message, String path, String correlationId) {
        return of(code, message, path, correlationId, null);
    }
}
