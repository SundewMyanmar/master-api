package com.sdm.core.exception;

import org.springframework.http.HttpStatus;

public class GeneralException extends RuntimeException {
    private HttpStatus status;

    public GeneralException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
