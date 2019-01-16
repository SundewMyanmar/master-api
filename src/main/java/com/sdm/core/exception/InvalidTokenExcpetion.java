package com.sdm.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidTokenExcpetion extends AuthenticationException {
    public InvalidTokenExcpetion(String message) {
        super(message);
    }
}
