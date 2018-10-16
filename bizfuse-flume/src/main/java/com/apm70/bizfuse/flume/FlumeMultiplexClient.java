package com.apm70.bizfuse.flume;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FlumeMultiplexClient implements FlumeRpcClient {
    private final String SHARDIN_GCOUNT = "sharding-count";
    private final int shardingCount;
    private final FlumeSingleClient[] clients;
    private final AtomicInteger count = new AtomicInteger();

    public FlumeMultiplexClient(final FlumeRpcClientProperties prop) throws IOException {

        this.shardingCount = Integer.parseInt(prop.getFlumeProp().getProperty(this.SHARDIN_GCOUNT, "2"));
        prop.getFlumeProp().remove(this.SHARDIN_GCOUNT);
        this.clients = new FlumeSingleClient[this.shardingCount];
        for (int i = 0; i < this.shardingCount; i++) {
            final FlumeRpcClientProperties newProp = new FlumeRpcClientProperties();
            this.copy(prop, newProp);
            newProp.setStorePath(prop.getStorePath() + File.separator + i);
            this.clients[i] = new FlumeSingleClient(newProp);
        }
    }

    @Override
    public void send(final byte[] msg) {
        this.send(msg, null);
    }

    @Override
    public void send(final byte[] msg, final Map<String, String> header) {
        if ((msg == null) || (msg.length == 0)) {
            return;
        }
        this.selectClient(header).send(msg, header);
    }

    @Override
    public void sendBatch(final List<byte[]> msgs) {
        msgs.stream().forEach(this::send);
    }

    @Override
    public void start() {
        for (final FlumeSingleClient client : this.clients) {
            client.start();
        }
    }

    @Override
    public void stop() {
        for (final FlumeSingleClient client : this.clients) {
            client.stop();
        }
    }

    private FlumeSingleClient selectClient(final Map<String, String> header) {
        int c = this.count.incrementAndGet();
        if (c < 0) {
            c = 0;
            this.count.set(c);
        }
        int index = 0;
        if (header == null) {
            index = c % this.shardingCount;
        } else {
            final String hValue = header.values().iterator().next();
            index = hValue.hashCode() % this.shardingCount;
        }
        return this.clients[index];
    }

    private void copy(final FlumeRpcClientProperties from, final FlumeRpcClientProperties to) {
        to.setFlumeProp(from.getFlumeProp());
        to.setStorePath(from.getStorePath());
    }
}
