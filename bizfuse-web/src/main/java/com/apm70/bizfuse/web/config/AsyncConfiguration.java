package com.apm70.bizfuse.web.config;

import java.util.concurrent.Executor;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.aop.interceptor.SimpleAsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.apm70.bizfuse.web.async.ExceptionHandlingAsyncTaskExecutor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@Configuration
@ConditionalOnProperty(name="bizfuse.web.async.enabled", havingValue="true", matchIfMissing=false)
public class AsyncConfiguration implements AsyncConfigurer {


    @Autowired
    private BizfuseWebProperties properties;
    /**
     * 需要对线程执行做包装的，可以通过实现该接口做到
     */
    @Autowired(required=false)
    private TaskDecorator taskDecorator;

    @Override
    @Bean(name = "taskExecutor")
    public Executor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setTaskDecorator(taskDecorator);
        executor.setCorePoolSize(this.properties.getAsync().getCorePoolSize());
        executor.setMaxPoolSize(this.properties.getAsync().getMaxPoolSize());
        executor.setQueueCapacity(this.properties.getAsync().getQueueCapacity());
        executor.setThreadNamePrefix("Async-Executor-");
        Executor asyncExecutor = new ExceptionHandlingAsyncTaskExecutor(executor);
        log.info(Constants.CONFIG_LOG_MARK, "Async enabled");
        return asyncExecutor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return new SimpleAsyncUncaughtExceptionHandler();
    }
}
