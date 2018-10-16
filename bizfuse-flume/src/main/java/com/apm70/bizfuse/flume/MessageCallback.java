package com.apm70.bizfuse.flume;

import java.util.List;

public interface MessageCallback<Message> {
    boolean process(List<Message> messages);
}
