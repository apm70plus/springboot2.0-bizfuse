package com.apm70.bizfuse.flume;

import java.io.File;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

import com.apm70.bizfuse.flume.FileStoreQueue;

public class FileStoreQueueTest {

    private static FileStoreQueue queue;

    static {
        try {
            final File store = new File("/home/liuyg/store.dat");
            final File meta = new File("/home/liuyg/meta.dat");
            FileStoreQueueTest.queue = new FileStoreQueue(meta, store);
        } catch (final Exception e) {
        }
    }

    @Test
    public void testAdd() throws InterruptedException {
        final String msg = "文件存储队列的持久化消息测试：";
        for (int i = 1; i <= 10; i++) {
            final byte[] bytes = (msg + i).getBytes(Charset.forName("UTF-8"));
            FileStoreQueueTest.queue.add(bytes);
        }
        FileStoreQueueTest.queue.batchPoll().stream().map(String::new).forEach(System.out::println);
    }

    @Test
    public void testPoll() throws InterruptedException {
        final String msg = "文件存储队列的持久化消息测试：";
        for (int i = 1; i <= 10; i++) {
            final byte[] bytes = (msg + i).getBytes(Charset.forName("UTF-8"));
            FileStoreQueueTest.queue.add(bytes);
            final byte[] value = FileStoreQueueTest.queue.poll();
            Assert.assertEquals(msg + i, new String(value, Charset.forName("UTF-8")));
        }
    }

    @Test
    public void testBatchPoll() throws InterruptedException {
        final String msg = "文件存储队列的持久化消息测试：";
        for (int i = 1; i <= 10; i++) {
            final byte[] bytes = (msg + i).getBytes(Charset.forName("UTF-8"));
            FileStoreQueueTest.queue.add(bytes);
        }
        final AtomicInteger i = new AtomicInteger(0);
        FileStoreQueueTest.queue.batchPoll().stream().map(String::new).forEach(m -> {
            Assert.assertEquals(msg + i.incrementAndGet(), m);
        });
    }

    @Test
    public void testBatchOffer() throws InterruptedException {
        final String msg = "文件存储队列的持久化消息测试：";
        for (int i = 1; i <= 10; i++) {
            final byte[] bytes = (msg + i).getBytes(Charset.forName("UTF-8"));
            FileStoreQueueTest.queue.add(bytes);
        }
        final AtomicInteger i = new AtomicInteger(0);
        FileStoreQueueTest.queue.batchPeek().stream().map(String::new).forEach(m -> {
            Assert.assertEquals(msg + i.incrementAndGet(), m);
        });
        i.set(0);
        FileStoreQueueTest.queue.batchPeek().stream().map(String::new).forEach(m -> {
            Assert.assertEquals(msg + i.incrementAndGet(), m);
        });
        FileStoreQueueTest.queue.batchPoll();
    }
}
