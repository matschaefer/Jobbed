package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/**
 * Fehlendes, abgelaufenes, widerrufenes oder wiederverwendetes Refresh-Token.
 * Führt zu HTTP 401, damit das Frontend zur Anmeldung weiterleitet.
 */
public class SessionExpiredException extends ApiException {

    public SessionExpiredException() {
        super(ErrorCode.AUTHENTICATION_REQUIRED, "Die Sitzung ist abgelaufen. Bitte erneut anmelden.");
    }
}
