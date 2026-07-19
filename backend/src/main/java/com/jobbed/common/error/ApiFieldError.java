package com.jobbed.common.error;

/**
 * Feldbezogener Validierungsfehler innerhalb von {@link ApiError}.
 */
public record ApiFieldError(String field, String message) {
}
