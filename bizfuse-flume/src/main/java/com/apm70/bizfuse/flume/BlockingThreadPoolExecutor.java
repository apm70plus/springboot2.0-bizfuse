package com.apm70.bizfuse.flume;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 带阻塞功能的线程池，当任务队列满时，阻塞调用
 *
 * @author liuyg
 */
public class BlockingThreadPoolExecutor extends ThreadPoolExecutor {

    public BlockingThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime,
            final TimeUnit unit,
            final BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    @Override
    public void execute(final Runnable command) {
        while (this.getQueue().remainingCapacity() < 10) {
            try {
                Thread.sleep(2);
            } catch (final InterruptedException e) {
            }
        }
        super.execute(command);
    }
}
