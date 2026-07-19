package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/**
 * Wird geworfen, wenn eine Operation gegen eine Eindeutigkeits- oder
 * Zustandsbedingung verstößt (z. B. E-Mail bereits vergeben, Firma mit
 * Bewerbungen löschen).
 */
public class ResourceConflictException extends ApiException {

    public ResourceConflictException(String message) {
        super(ErrorCode.RESOURCE_CONFLICT, message);
    }
}
