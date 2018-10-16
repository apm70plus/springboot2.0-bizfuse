package com.apm70.bizfuse.event;

import java.io.IOException;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apm70.bizfuse.util.Jacksons;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

/**
 * MQ 消息驱动的事件监听器
 * 
 * @author liuyg
 */
@Component
public class MqEventDrivenListener implements MessageListener {
	
	@Autowired
	private EventDrivenSubscriber eventDrivenSubscriber;
	
	@Override
	public void onMessage(Message message) {
		try {
			Event event = Jacksons.getMapper().readValue(message.getBody(), Event.class);
			eventDrivenSubscriber.persistAndHandleMessage(event.getBusinessType(), event.getPayload(), event.getGuid());
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
