package com.apm70.bizfuse.event;

import java.time.OffsetDateTime;
import java.util.List;

import com.apm70.bizfuse.domain.PublishedEvent;
import com.apm70.bizfuse.enums.EventStatus;
import com.apm70.bizfuse.repository.PublishedEventRepository;

/**
 * 批量获取发布超时未应答的消息列表，以便重新发送
 * 
 * @author 
 */
public enum RepublishPendingEventStrategy implements BatchFetchEventStrategy {
    SINGLETON;

    @Override
    public List<PublishedEvent> execute(PublishedEventRepository repository) {
        // 取出3秒前已经发送过至队列但是没有收到ack请求的消息，并进行重试
        return repository.findTop100ByEventStatusAndLastModifiedDateBefore(EventStatus.PENDING, OffsetDateTime.now().minusSeconds(3).toLocalDateTime());
    }
}
