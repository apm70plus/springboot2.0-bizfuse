package com.apm70.bizfuse.flume;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.flume.Event;
import org.apache.flume.EventDeliveryException;
import org.apache.flume.api.RpcClient;
import org.apache.flume.api.RpcClientFactory;
import org.apache.flume.event.EventBuilder;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

/**
 * Flume的RPC客户端，支持本地文件缓存，防止文件丢失
 *
 * @author liuyg
 */
@Slf4j
public class FlumeSingleClient implements FlumeRpcClient {

    /**
     * 重连间隔15秒
     */
    private final long reconnInterval = 15000;
    /**
     * 最后一次连接时间
     */
    private long latestConnTime;
    /**
     * Flume的RPC客户端
     */
    private RpcClient client;
    /**
     * Flume的RPC客户端配置
     */
    private final FlumeRpcClientProperties prop;
    /**
     * 基于文件存储的消息队列
     */
    private final FileStoreQueue queue;
    /**
     * 消息的信号队列
     */
    private final ArrayBlockingQueue<Boolean> msgSignals = new ArrayBlockingQueue<>(10);

    private volatile boolean stoped = false;

    private static final byte HEADER_TAG = 0x7E;

    public FlumeSingleClient(final FlumeRpcClientProperties prop) throws IOException {
        this.prop = prop;
        final File storeDir = new File(prop.getStorePath());
        if (!storeDir.exists()) {
            storeDir.mkdirs();
        }
        final File storeFile = new File(prop.getStorePath(), "store.dat");
        final File metaFile = new File(prop.getStorePath(), "meta.dat");
        this.queue = new FileStoreQueue(metaFile, storeFile);
        this.queue.setBatchSize(Integer.parseInt(prop.getFlumeProp().getProperty("batch-size")));
    }

    @Override
    public void start() {
        this.connectFlume();
        this.startSendingMsg();
    }

    @Override
    public void stop() {
        this.stoped = true;
        this.fireMsgSignal();
        if (this.client != null) {
            this.client.close();
            this.client = null;
        }
        this.queue.close();
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
        this.queue.add(this.encode(msg, header));
        if (this.queue.size() > 20000) {
            FlumeSingleClient.log.info("Flume client local store messages {}", this.queue.size());
        }
        this.fireMsgSignal();
    }

    @Override
    public void sendBatch(final List<byte[]> msgs) {
        msgs.stream().forEach(this.queue::add);
        this.fireMsgSignal();
    }

    private void startSendingMsg() {
        this.stoped = false;
        final Thread sendingTask = new Thread(() -> {
            while (!FlumeSingleClient.this.stoped) {
                FlumeSingleClient.this.send2Flume();
            }
        });
        sendingTask.setPriority(Thread.MAX_PRIORITY);
        sendingTask.start();
        this.fireMsgSignal();
    }

    private void send2Flume() {
        if (this.client == null) {
            this.connectFlume();
            return;
        }
        try {
            this.msgSignals.poll(10, TimeUnit.SECONDS);
            final long startWait = System.currentTimeMillis();
            while ((this.queue.size() < 100) && ((System.currentTimeMillis() - startWait) < 10L)) {
                Thread.sleep(1L);
            }
            FlumeSingleClient.log.debug("队列里剩余消息数量 {}", this.queue.size());
            do {
                final List<byte[]> msgs = this.queue.batchPeek();
                if (msgs == null) {
                    break;
                }
                try {
                    final List<Event> events = msgs.stream().map(this::decode).collect(Collectors.toList());
                    this.client.appendBatch(events);
                    this.queue.batchPoll();
                } catch (final EventDeliveryException e) {
                    // 发送失败， 可能是连接问题，重连
                    FlumeSingleClient.log.warn("Flume 消息发送失败，重试");
                    this.connectFlume();
                }
            } while (this.queue.size() >= 100);
        } catch (final Throwable e) {
            FlumeSingleClient.log.error("发送Flume消息失败", e);
        }
    }

    private void connectFlume() {
        if ((System.currentTimeMillis() - this.latestConnTime) < this.reconnInterval) {
            return;
        }
        try {
            if (this.client != null) {
                this.client.close();
            }
            this.latestConnTime = System.currentTimeMillis();
            //this.client = RpcClientFactory.getDefaultInstance("172.17.6.247", 60001, 500);
            this.client = RpcClientFactory.getInstance(this.prop.getFlumeProp());
            FlumeSingleClient.log.info("建立Flume连接完成");
        } catch (final Exception e) {
            FlumeSingleClient.log.warn("flume 连接失败...", e);
        }
    }

    private void fireMsgSignal() {
        if (this.msgSignals.isEmpty()) {
            this.msgSignals.add(Boolean.TRUE);
        }
    }

    private byte[] encode(final byte[] body, final Map<String, String> header) {
        if ((header == null) || header.isEmpty()) {
            return FlumeSingleClient.escape(body);
        }
        final byte[] bHeader = JSON.toJSONBytes(header);
        if (bHeader.length > 128) {
            throw new RuntimeException("Header 长度超长，最多128字节：" + new String(bHeader));
        }
        final byte[] headerAndBody = new byte[body.length + bHeader.length + 2];
        headerAndBody[0] = FlumeSingleClient.HEADER_TAG;
        headerAndBody[1] = (byte) bHeader.length;
        System.arraycopy(bHeader, 0, headerAndBody, 2, bHeader.length);
        System.arraycopy(body, 0, headerAndBody, 2 + bHeader.length, body.length);
        return headerAndBody;
    }

    private Event decode(final byte[] msg) {
        if (msg[0] != FlumeSingleClient.HEADER_TAG) {
            final byte[] body = FlumeSingleClient.unEscape(msg);
            return EventBuilder.withBody(body);
        }
        final ByteBuffer buffer = ByteBuffer.wrap(msg);
        buffer.get();
        final byte[] header = new byte[buffer.get()];
        buffer.get(header);
        @SuppressWarnings("unchecked")
        final Map<String, String> headerMap = (Map<String, String>) JSON.parse(header);
        final byte[] body = new byte[buffer.remaining()];
        buffer.get(body);
        return EventBuilder.withBody(body, headerMap);
    }

    /**
     * 加入标示符的转义进行封装
     *
     * @param data
     * @return
     */
    private static byte[] escape(final byte[] data) {
        if (data[0] == FlumeSingleClient.HEADER_TAG) {
            final byte[] target = new byte[data.length + 1];
            target[0] = FlumeSingleClient.HEADER_TAG - 1;
            target[1] = (byte) 0x02;
            System.arraycopy(data, 1, target, 2, data.length - 1);
            return target;
        } else if (data[0] == (FlumeSingleClient.HEADER_TAG - 1)) {
            final byte[] target = new byte[data.length + 1];
            target[0] = data[0];
            target[1] = (byte) 0x01;
            System.arraycopy(data, 1, target, 2, data.length - 1);
            return target;
        } else {
            return data;
        }
    }

    /**
     * 将标识字符的转义字符还原
     *
     * @param data
     * @return
     */
    private static byte[] unEscape(final byte[] data) {
        if (data[0] == (FlumeSingleClient.HEADER_TAG - 1)) {
            final byte[] target = new byte[data.length - 1];
            if (data[1] == 0x01) {
                target[0] = data[0];
            } else {
                target[0] = FlumeSingleClient.HEADER_TAG;
            }
            System.arraycopy(data, 2, target, 1, data.length - 2);
            return target;
        } else {
            return data;
        }
    }
}
