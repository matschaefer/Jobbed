package com.jobbed.common.error.exception;

import com.jobbed.common.error.ErrorCode;

/**
 * Wird geworfen, wenn eine fachliche Regel verletzt wird (HTTP 422).
 */
public class BusinessRuleException extends ApiException {

    public BusinessRuleException(String message) {
        super(ErrorCode.BUSINESS_RULE_VIOLATION, message);
    }
}
