package com.sokima.executor.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class ConcurrentConfiguration {

    @Value("${executor.scheduled-pool.core-size}")
    private int scheduledCorePoolSize;

    @Value("${executor.common-pool.core-size}")
    private int commonCorePoolSize;

    @Bean
    @Qualifier("scheduledExecutorService")
    public ScheduledExecutorService scheduledExecutorService() {
        return Executors.newScheduledThreadPool(scheduledCorePoolSize);
    }

    /**
     * For {@link CompletableFuture} use only.
     * Needed to prevent issue with common fork join pool.
     *
     * <a href="https://stackoverflow.com/questions/21163108/custom-thread-pool-in-java-8-parallel-stream">see possible issue</a>
     */
    @Bean
    @Qualifier("commonCompletableFutureExecutorService")
    public ExecutorService commonCompletableFutureExecutorService() {
        return Executors.newFixedThreadPool(commonCorePoolSize);
    }
}
