package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/**
 * Wird geworfen, wenn eine Ressource nicht existiert – oder aus Gründen der
 * Mandantentrennung dem aktuellen Nutzer nicht zugeordnet ist (bewusst 404
 * statt 403, um die Existenz nicht zu verraten).
 */
public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String message) {
        super(ErrorCode.RESOURCE_NOT_FOUND, message);
    }

    public static ResourceNotFoundException of(String resource, Object id) {
        return new ResourceNotFoundException(resource + " mit ID " + id + " wurde nicht gefunden.");
    }
}
