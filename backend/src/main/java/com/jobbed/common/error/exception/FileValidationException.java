package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

public class FileValidationException extends ApiException {
    public FileValidationException(ErrorCode code, String message) { super(code, message); }
}
