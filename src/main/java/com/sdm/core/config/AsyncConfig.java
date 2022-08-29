package com.sdm.core.config;

import com.sdm.core.exception.AsyncException;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import lombok.extern.log4j.Log4j2;

@Configuration
@EnableAsync
@Log4j2
public class AsyncConfig implements AsyncConfigurer {

    private static final int POOL_SIZE = 3;

    private static final int MAX_POOL_SIZE = 5;

    private static final int MAX_QUEUE_SIZE = 100;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(MAX_QUEUE_SIZE);
        executor.setThreadNamePrefix("Background_process_");
        executor.initialize();
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new AsyncException();
    }
}
