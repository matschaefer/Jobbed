package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

public class EmailDeliveryException extends ApiException {

    public EmailDeliveryException() {
        super(ErrorCode.EMAIL_DELIVERY_FAILED,
                "Die E-Mail konnte gerade nicht versendet werden. Bitte versuche es später erneut.");
    }
}
