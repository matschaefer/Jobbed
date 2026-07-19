package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/** Ungültiger oder abgelaufener Einmal-Token (E-Mail-Verifikation / Passwort-Reset). */
public class InvalidTokenException extends ApiException {

    public InvalidTokenException(String message) {
        super(ErrorCode.INVALID_TOKEN, message);
    }
}
