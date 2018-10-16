package com.apm70.bizfuse.event;

import java.util.List;

import com.apm70.bizfuse.domain.PublishedEvent;
import com.apm70.bizfuse.repository.PublishedEventRepository;

/**
 * 批量获取消息的接口（基于策略模式）
 * 
 * @author 
 */
public interface BatchFetchEventStrategy {
    List<PublishedEvent> execute(PublishedEventRepository repository);
}
