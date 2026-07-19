package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/** Zugriff verweigert (z. B. deaktiviertes Konto). HTTP 403. */
public class ForbiddenException extends ApiException {

    public ForbiddenException(String message) {
        super(ErrorCode.ACCESS_DENIED, message);
    }
}
