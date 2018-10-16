package com.apm70.bizfuse.flume;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

import com.apm70.bizfuse.flume.FlumeRpcClientProperties;
import com.apm70.bizfuse.flume.FlumeSingleClient;

class FlumeRpcClientTest {

    public static void main(final String[] args) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);

        final FlumeRpcClientProperties clientProp = new FlumeRpcClientProperties();
        clientProp.setStorePath("/tmp");
        final Properties prop = new Properties();
        prop.setProperty("client.type", "default_loadbalance");
        prop.setProperty("hosts", "h1 h2");
        prop.setProperty("hosts.h1", "172.17.7.107:60001");
        prop.setProperty("hosts.h2", "172.17.7.107:60002");
        prop.setProperty("batch-size", "500");
        //prop.setProperty("hosts.h3", "172.17.6.247:60003");
        clientProp.setFlumeProp(prop);

        final FlumeSingleClient client = new FlumeSingleClient(clientProp);
        client.start();
        System.out.println("开始发送数据：" + System.currentTimeMillis());
        final String msg = "Flume Client Test Message";
        final Map<String, String> header = new HashMap<>();
        header.put("TCS", "tcs1");
        for (int i = 0; i < 10000; i++) {
            if ((i % 2) == 0) {
                client.send(msg.getBytes());
            } else {
                client.send(msg.getBytes(), header);
            }
        }
        System.out.println("数据发送完成：" + System.currentTimeMillis());
        latch.await();
        client.stop();
    }

}
