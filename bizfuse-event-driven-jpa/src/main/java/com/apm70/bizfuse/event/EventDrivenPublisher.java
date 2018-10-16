package com.apm70.bizfuse.event;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.Charsets;
import org.assertj.core.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.apm70.bizfuse.domain.PublishedEvent;
import com.apm70.bizfuse.enums.EventStatus;
import com.apm70.bizfuse.identity.GuidGenerator;
import com.apm70.bizfuse.repository.PublishedEventRepository;
import com.apm70.bizfuse.util.HibernateValidators;
import com.apm70.bizfuse.util.Jacksons;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

/**
 * 事件驱动架构的“消息发布者”
 * 
 * @author liuyg
 */
@Component
public class EventDrivenPublisher {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventDrivenPublisher.class);

	@Autowired
	private PublishedEventRepository publishedEventRepository;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	@Autowired(required=false)
	private MessageRouteFacotry messageRouteFacotry;
	
	private Cache<String, Event> needAckEvents = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.SECONDS).maximumSize(10000).build();
	
	private static final ConcurrentMap<String, MessageRoute> REGISTRY = new ConcurrentHashMap<>();

	/**
	 * the basic.return is sent to the client before basic.ack
	 */
	@PostConstruct
	public void postConstruct() {
		// return
		rabbitTemplate.setReturnCallback(new RabbitReturnCallback());
		// ack
		rabbitTemplate.setConfirmCallback(new RabbitConfirmCallback());
	}

	/**
	 * 扫描定量的NEW事件，发布至Broker之后更新为PENDING
	 */
	@Scheduled(fixedRate = 1000)
	public void fetchAndPublishEventInNewStatus() {
		fetchAndPublishToBroker(PublishNewEventStrategy.SINGLETON);
	}

	/**
	 * 扫面定量的PENDING事件并重新发布至Broker，意在防止实例因为意外宕机导致basic.return和basic.ack的状态丢失。
	 */
	@Scheduled(fixedRate = 5000)
	public void fetchAndRepublishEventInPendingStatus() {
		fetchAndPublishToBroker(RepublishPendingEventStrategy.SINGLETON);
	}

	/**
	 * 根据传入的业务类型取出所设定的exchange与routeKey
	 */
	public MessageRoute getMessageRoute(String businessType) {
		Preconditions.checkNotNull(businessType);
		MessageRoute route = REGISTRY.get(businessType);
		if (route != null) {
			return route;
		} else if (messageRouteFacotry!= null) {
			route = messageRouteFacotry.get(businessType);
			if (route != null) {
				REGISTRY.put(businessType, route);
			}
		}
		return route;
	}

	/**
	 * 所有的业务类型都必须先注册exchange与routeKey才能使用，而不是将exchange与routeKey持久化，浪费大量磁盘空间。
	 */
	public static void registerType(String businessType, String exchange, String routeKey) {
		Preconditions.checkNotNull(businessType);
		Preconditions.checkNotNull(exchange);
		Preconditions.checkNotNull(routeKey);
		REGISTRY.put(businessType, new MessageRoute(exchange, routeKey));
	}

	/**
	 * 判断业务类型是否有被注册
	 */
	public static boolean includesType(String businessType) {
		Preconditions.checkNotNull(businessType);
		return REGISTRY.containsKey(businessType);
	}

	public static void throwIfNotIncluded(String businessType) {
		Preconditions.checkNotNull(businessType);
		Preconditions.checkArgument(includesType(businessType), "该业务类型尚未注册");
	}

	/**
	 * 基于Transaction，消息先落地，之后通过定时扫描发布数据至Broker
	 */
	@Transactional(rollbackFor = Exception.class)
	public void persistAndPublish(Object payload, String businessType) {
		Preconditions.checkNotNull(payload);
		Preconditions.checkNotNull(businessType);
		final PublishedEvent publisher = new PublishedEvent();
		publisher.setEventStatus(EventStatus.NEW);
		publisher.setGuid(GuidGenerator.generate());
		publisher.setPayload(Jacksons.parse(payload));
		publisher.setBusinessType(businessType);
		publishedEventRepository.save(publisher);
	}
	
	/**
	 * 直接发送至Broker
	 * 
	 * @param payload
	 * @param businessType
	 */
	public void publish(Object payload, String businessType) {
		final MessageRoute route = getMessageRoute(businessType);
		if (route != null) {
			// 发送
			// DTO转换
			final Event dto = new Event();
			dto.setBusinessType(businessType);
			dto.setGuid(GuidGenerator.generate());
			dto.setPayload(Jacksons.parse(payload));
			// 正式发送至Broker
			publish(dto, route.getExchange(), route.getRouteKey(),
					new CorrelationData(String.valueOf(dto.getGuid())));
			
		} else {
			// 将event status置为FAILED，等待人工处理
			LOGGER.warn("事件尚未注册不能被发送至Broker");
		}
	}

	/**
	 * 直接发布消息至Broker，通常由定时器扫描发送
	 */
	private void publish(Event event, String exchange, String routeKey, CorrelationData correlationData) {
		Preconditions.checkNotNull(event);
		Preconditions.checkNotNull(exchange);
		Preconditions.checkNotNull(routeKey);
		HibernateValidators.throwsIfInvalid(event);
		rabbitTemplate.convertAndSend(exchange, routeKey, event, correlationData);
		needAckEvents.put(event.getGuid(), event);
	}

	/**
	 * 按照指定的策略将指定状态的事件(通常为NEW与PENDING)发布至Broker
	 */
	@Transactional(rollbackFor = Exception.class)
	public void fetchAndPublishToBroker(BatchFetchEventStrategy fetchStrategy) {
		Preconditions.checkNotNull(fetchStrategy);
		final List<PublishedEvent> events = fetchStrategy.execute(publishedEventRepository);
		for (PublishedEvent event : events) {
			final String type = event.getBusinessType();
			final MessageRoute route = getMessageRoute(type);
			if (route != null) {
				// 发送
				// DTO转换
				final Event dto = new Event();
				dto.setBusinessType(type);
				dto.setGuid(event.getGuid());
				dto.setPayload(event.getPayload());
				// 更新状态为'处理中'顺便刷新一下update_time
				event.setEventStatus(EventStatus.PENDING);
				// 意在多实例的情况下不要重复刷新
				publishedEventRepository.save(event);
				// 正式发送至Broker
				publish(dto, route.getExchange(), route.getRouteKey(),
						new CorrelationData(String.valueOf(event.getId())));
			} else {
				// 将event status置为FAILED，等待人工处理
				event.setEventStatus(EventStatus.FAILED);
				publishedEventRepository.save(event);
				LOGGER.warn("事件尚未注册不能被发送至Broker, id: {}, guid: {}，目前已将该事件置为FAILED，待审查过后人工将状态校正", event.getId(), event.getGuid());
			}
		}
	}

	/**
	 * 当exchange存在但无法路由至queue的情况下记录入库
	 * <p>
	 * basic.return（basic.return将会发生在basic.ack之前）
	 */
	private class RabbitReturnCallback implements RabbitTemplate.ReturnCallback {
		@Override
		public void returnedMessage(Message message, int replyCode, String replyText, String exchange,
				String routingKey) {
			final String failedMessage = new String(message.getBody(), Charsets.UTF_8);
			try {
				final String guid = Jacksons.getMapper().readTree(failedMessage).get("guid").asText();
				final PublishedEvent publisher = new PublishedEvent();
				publisher.setGuid(guid);
				if (EventStatus.NO_ROUTE.name().equalsIgnoreCase(replyText)) {
					publisher.setEventStatus(EventStatus.NO_ROUTE);
				} else {
					logReturnedFault(replyCode, replyText, exchange, routingKey, failedMessage);
					publisher.setEventStatus(EventStatus.ERROR);
				}
				// 因为在basic.return之后会调用basic.ack，鄙人认为NO_ROUTE的状态有可能被错误地转换成为NOT_FOUND，所以不需要考虑竞争情况
				publishedEventRepository.save(publisher);
			} catch (Exception e) {
				logReturnedFault(replyCode, replyText, exchange, routingKey, failedMessage);
			}
		}

		private void logReturnedFault(int replyCode, String replyText, String exchange, String routingKey,
				String failedMessage) {
			LOGGER.error("no route for message and failed to read it: {}, replyCode: {}, replyText: {}, "
					+ "exchange: {}, routeKey: {}", failedMessage, replyCode, replyText, exchange, routingKey);
		}
	}

	/**
	 * 确认Broker接收消息的状态
	 * <p>
	 * basic.ack
	 */
	private class RabbitConfirmCallback implements RabbitTemplate.ConfirmCallback {
		@Override
		public void confirm(CorrelationData correlationData, boolean ack, String cause) {
			if (isGuid(correlationData.getId())) {
				Event memoryEvent = needAckEvents.getIfPresent(correlationData.getId());
				if (ack) {
					LOGGER.info("消息成功送达，BusinessType: {}, GUID: {}", memoryEvent.getBusinessType(), correlationData.getId());
				} else {
					LOGGER.info("消息发送失败，BusinessType: {}, GUID: {}", memoryEvent.getBusinessType(), correlationData.getId());
					// TODO：记录到数据库，尝试重发
				}
				if (memoryEvent != null) {
					needAckEvents.invalidate(correlationData.getId());
					// TODO: 记录到数据库
				}
				return;
			}
			
			final Long id = Long.valueOf(correlationData.getId());
			// 当一条消息为PENDING而且ack为true时则删除原有的消息}
			if (ack) {
				// 或直接删除
				Optional<PublishedEvent> optional = publishedEventRepository.findById(id);
				optional.ifPresent(event -> {
					event.setEventStatus(EventStatus.DONE);
					publishedEventRepository.save(optional.get());
				});
				//publishedEventRepository.deleteById(id);
			} else {
				Optional<PublishedEvent> optional = publishedEventRepository.findById(id);
				optional.ifPresent(event -> {
					event.setEventStatus(EventStatus.NOT_FOUND);
					publishedEventRepository.save(event);
				});
				// 打开mandatory之后，ack为false的情况就是没有找到exchange
				LOGGER.error("message has failed to found a proper exchange which local id is {}. cause: {}", id, cause);
			}
		}
	}
	
	private boolean isGuid(String value) {
		return value != null && value.length() == 36;
	}

}
