package com.apm70.bizfuse.flume;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.AvroRemoteException;
import org.apache.avro.ipc.NettyServer;
import org.apache.avro.ipc.Server;
import org.apache.avro.ipc.specific.SpecificResponder;
import org.apache.flume.source.avro.AvroFlumeEvent;
import org.apache.flume.source.avro.AvroSourceProtocol;
import org.apache.flume.source.avro.Status;
import org.codehaus.jackson.map.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FlumeAvroRpcServer<Message> {
    private final Server flumeNettyServer;
    private final int port;
    private final MessageCallback<Message> messageCallback;
    private final Class<Message> messageClazz;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FlumeAvroRpcServer(final int port, final MessageCallback<Message> messageCallback,
            final Class<Message> messageClazz) {

        this.port = port;
        this.messageCallback = messageCallback;
        this.messageClazz = messageClazz;
        this.flumeNettyServer = this.start();
    }

    private Server start() {
        return new NettyServer(new SpecificResponder(AvroSourceProtocol.class, new AvroSourceProtocol() {

            @Override
            public Status append(final AvroFlumeEvent event) throws AvroRemoteException {
                final Message message = this.convert(event);

                if (FlumeAvroRpcServer.log.isDebugEnabled()) {
                    FlumeAvroRpcServer.log.debug("AvroFlumeServer: 收到一条新消息。\n" + message.toString());
                }

                final boolean isSuccess = FlumeAvroRpcServer.this.messageCallback.process(
                        Arrays.asList(message));
                return isSuccess ? Status.OK : Status.FAILED;
            }

            @Override
            public Status appendBatch(final List<AvroFlumeEvent> events) throws AvroRemoteException {
                final List<Message> messages =
                        events.stream().map(event -> this.convert(event)).filter(msg -> msg != null)
                                .collect(Collectors.toList());
                if (messages.isEmpty()) {
                    return Status.OK;
                }

                if (FlumeAvroRpcServer.log.isDebugEnabled()) {
                    final StringBuilder sb = new StringBuilder();
                    sb.append("AvroFlumeServer: 收到一批（" + messages.size() + "）新消息。\n");
                    messages.forEach(message -> sb.append(message.toString()).append("\n"));
                    FlumeAvroRpcServer.log.debug(sb.toString());
                }

                final boolean isSuccess =
                        FlumeAvroRpcServer.this.messageCallback.process(messages);
                return isSuccess ? Status.OK : Status.FAILED;
            }

            @SuppressWarnings("unchecked")
            private Message convert(final AvroFlumeEvent event) {
                final ByteBuffer body = event.getBody();
                final int len = body.remaining();
                final byte[] buffer = new byte[len];
                try {
                    body.get(buffer);
                    if (FlumeAvroRpcServer.this.messageClazz == byte[].class) {
                        return (Message) buffer;
                    }
                    if (FlumeAvroRpcServer.this.messageClazz == String.class) {
                        return (Message) (new String(buffer));
                    }
                    final Message msg = FlumeAvroRpcServer.this.objectMapper.readValue(buffer, 0, len,
                            FlumeAvroRpcServer.this.messageClazz);
                    return msg;
                } catch (final Exception e) {
                    FlumeAvroRpcServer.log.error("消息转换失败：{}", new String(buffer, 0, len));
                    return null;
                } finally {
                    body.clear();
                }
            }
        }), new InetSocketAddress(this.port));
    }

    public void stop() {
        this.flumeNettyServer.close();
        FlumeAvroRpcServer.log.info("FlumeAvroRpcServer is going to stop.");
    }
}
