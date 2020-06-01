package com.sdm.core.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

@Log4j2
public class AsyncException implements AsyncUncaughtExceptionHandler {
    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        log.warn(throwable.getLocalizedMessage(), throwable);
    }
}
