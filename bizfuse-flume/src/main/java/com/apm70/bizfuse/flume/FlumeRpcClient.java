package com.apm70.bizfuse.flume;

import java.util.List;
import java.util.Map;

public interface FlumeRpcClient {

    void start();

    void stop();

    void send(final byte[] msg, final Map<String, String> header);

    void send(final byte[] msg);

    void sendBatch(final List<byte[]> msgs);
}
