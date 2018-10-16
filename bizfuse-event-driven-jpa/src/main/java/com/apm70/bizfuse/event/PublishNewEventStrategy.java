package com.apm70.bizfuse.event;

import java.util.List;

import com.apm70.bizfuse.domain.PublishedEvent;
import com.apm70.bizfuse.enums.EventStatus;
import com.apm70.bizfuse.repository.PublishedEventRepository;

/**
 * 批量获取未处理消息的实现类
 * 
 * @author 
 */
public enum PublishNewEventStrategy implements BatchFetchEventStrategy {
    SINGLETON;

    @Override
    public List<PublishedEvent> execute(PublishedEventRepository repository) {
        return repository.findTop100ByEventStatus(EventStatus.NEW);
    }
}
