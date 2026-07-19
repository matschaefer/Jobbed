package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/**
 * Basisklasse für fachliche Ausnahmen, die vom zentralen Exception-Handler in
 * das einheitliche Fehlerformat übersetzt werden.
 */
public abstract class ApiException extends RuntimeException {

    private final ErrorCode errorCode;

    protected ApiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
