package com.apm70.bizfuse.event;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.apm70.bizfuse.domain.SubscribedEvent;
import com.apm70.bizfuse.enums.EventStatus;
import com.apm70.bizfuse.repository.SubscribedEventRepository;
import com.apm70.bizfuse.util.HibernateValidators;
import com.google.common.base.Preconditions;

import lombok.NonNull;

/**
 * 事件驱动架构的“消息消费者”。其实是集中接收消息，并根据BusinessType转发给特定消息处理器（EventHandler）
 * 
 * @author liuyg
 */
@Component
public class EventDrivenSubscriber {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventDrivenSubscriber.class);

	private static final Map<String, EventHandler> HANDLERS = new HashMap<>();

	/**
	 * 注册消息的处理器
	 * 
	 * @param handler
	 * @param businessType
	 */
	public static void register(@NonNull EventHandler handler, @NonNull String businessType) {
		HANDLERS.put(businessType, handler);
	}

	@Autowired
	private SubscribedEventRepository subscriberRepository;

	/**
	 * 持久化并处理接收到的消息
	 * 
	 * @param businessType 消息类型
	 * @param payload      消息体
	 * @param guid         唯一ID（为实现幂等性）
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public int persistAndHandleMessage(String businessType, String payload, String guid) {
		Preconditions.checkNotNull(businessType);
		Preconditions.checkNotNull(payload);
		Preconditions.checkNotNull(guid);
		final SubscribedEvent subscriber = new SubscribedEvent();
		subscriber.setBusinessType(businessType);
		subscriber.setPayload(payload);
		subscriber.setGuid(guid);
		subscriber.setEventStatus(EventStatus.NEW);
		HibernateValidators.throwsIfInvalid(subscriber);
		int influence = 0;
		try {
			subscriberRepository.save(subscriber);
		} catch (Exception e) {
			LOGGER.info("duplicate key in processing message '{}'", guid);
			return 0;
		}
		// 非重复消息则执行实际的业务
		EventHandler handler = HANDLERS.get(businessType);
		if (handler != null) {
			try {
				handler.handle(payload);
			} catch (Exception e) {
				// TODO : 如果是乐观锁异常，则重试，否则标记错误，放弃处理
				LOGGER.error("消息处理失败！", e);
			}
			subscriber.setEventStatus(EventStatus.DONE);
			this.subscriberRepository.save(subscriber);
		} else {
			LOGGER.error(
					"event which id is {} has to change status from NEW to NOT_FOUND due to threr is not a match handler.",
					subscriber.getId());
			subscriber.setEventStatus(EventStatus.NOT_FOUND);
			subscriberRepository.save(subscriber);
		}
		return influence;
	}
}
