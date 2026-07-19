package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/** Login mit noch nicht bestätigter E-Mail-Adresse. HTTP 403. */
public class EmailNotVerifiedException extends ApiException {

    public EmailNotVerifiedException() {
        super(ErrorCode.EMAIL_NOT_VERIFIED,
                "Bitte bestätige zuerst deine E-Mail-Adresse.");
    }
}
