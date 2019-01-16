package com.sdm.core.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;

import java.lang.reflect.Method;

public class AsyncException implements AsyncUncaughtExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AsyncException.class);

    @Override
    public void handleUncaughtException(Throwable throwable, Method method, Object... objects) {
        logger.warn(throwable.getLocalizedMessage(), throwable);
    }
}
