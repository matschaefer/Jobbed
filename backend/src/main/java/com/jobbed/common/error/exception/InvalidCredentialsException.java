package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/** Ungültige Anmeldedaten (bewusst generische Meldung, keine Enumeration). */
public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        super(ErrorCode.INVALID_CREDENTIALS, "E-Mail-Adresse oder Passwort ist ungültig.");
    }
}
