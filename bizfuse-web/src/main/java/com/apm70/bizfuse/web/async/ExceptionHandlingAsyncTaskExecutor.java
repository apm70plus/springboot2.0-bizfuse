package com.apm70.bizfuse.web.async;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.AsyncTaskExecutor;

public class ExceptionHandlingAsyncTaskExecutor implements AsyncTaskExecutor,
        InitializingBean, DisposableBean {

    private final Logger log = LoggerFactory.getLogger(ExceptionHandlingAsyncTaskExecutor.class);

    private final AsyncTaskExecutor executor;

    public ExceptionHandlingAsyncTaskExecutor(final AsyncTaskExecutor executor) {
        this.executor = executor;
    }

    @Override
    public void execute(final Runnable task) {
        this.executor.execute(this.createWrappedRunnable(task));
    }

    @Override
    public void execute(final Runnable task, final long startTimeout) {
        this.executor.execute(this.createWrappedRunnable(task), startTimeout);
    }

    private <T> Callable<T> createCallable(final Callable<T> task) {
        return () -> {
            try {
                return task.call();
            } catch (final Exception e) {
                this.handle(e);
                throw e;
            }
        };
    }

    private Runnable createWrappedRunnable(final Runnable task) {
        return () -> {
            try {
                task.run();
            } catch (final Exception e) {
                this.handle(e);
            }
        };
    }

    protected void handle(final Exception e) {
        this.log.error("Caught async exception", e);
    }

    @Override
    public Future<?> submit(final Runnable task) {
        return this.executor.submit(this.createWrappedRunnable(task));
    }

    @Override
    public <T> Future<T> submit(final Callable<T> task) {
        return this.executor.submit(this.createCallable(task));
    }

    @Override
    public void destroy() throws Exception {
        if (this.executor instanceof DisposableBean) {
            final DisposableBean bean = (DisposableBean) this.executor;
            bean.destroy();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.executor instanceof InitializingBean) {
            final InitializingBean bean = (InitializingBean) this.executor;
            bean.afterPropertiesSet();
        }
    }
}
