package com.sdm.payment.exception;

import lombok.Getter;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class CallbackException extends Exception {

    @Getter
    private final FailedType failedType;

    public CallbackException(FailedType type) {
        this.failedType = type;
    }

    public CallbackException(FailedType type, String message) {
        super(message);
        this.failedType = type;
    }

    public CallbackException(FailedType type, String message, Throwable cause) {
        super(message, cause);
        this.failedType = type;
    }
}
