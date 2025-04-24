package com.p3.resource_monitor.poc.schedular;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class SchedulerConfig {

    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);       // Minimum number of threads in the pool
        executor.setMaxPoolSize(10);       // Maximum number of threads in the pool
        executor.setQueueCapacity(100);    // Queue capacity for tasks
        executor.setThreadNamePrefix("scheduled-task-"); // Custom thread name prefix
        executor.initialize();
        return executor;
    }
}
