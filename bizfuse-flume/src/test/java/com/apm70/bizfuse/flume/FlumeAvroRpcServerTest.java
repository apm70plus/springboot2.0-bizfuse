package com.apm70.bizfuse.flume;

import java.util.concurrent.CountDownLatch;

import com.apm70.bizfuse.flume.FlumeAvroRpcServer;
import com.apm70.bizfuse.flume.MessageCallback;

public class FlumeAvroRpcServerTest {

    public static void main(final String[] args) throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final MessageCallback<String> messageListener = msg -> {
            //msg.stream().forEach(System.out::println);
            System.out.println("收到消息：" + msg.size());
            System.out.println(msg.get(0));
            return true;
        };

        final FlumeAvroRpcServer<String> flumeServer = new FlumeAvroRpcServer<>(60006, messageListener, String.class);
        latch.await();
        flumeServer.stop();
    }
}
