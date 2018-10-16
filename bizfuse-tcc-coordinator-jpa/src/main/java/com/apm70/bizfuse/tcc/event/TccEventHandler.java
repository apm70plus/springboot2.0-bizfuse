package com.apm70.bizfuse.tcc.event;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.apm70.bizfuse.event.EventHandler;
import com.apm70.bizfuse.tcc.config.Constants;
import com.apm70.bizfuse.tcc.domain.Participant;
import com.apm70.bizfuse.tcc.service.CoordinatorService;
import com.apm70.bizfuse.util.Jacksons;

import lombok.extern.slf4j.Slf4j;

/**
 * TCC事件处理器，处理TCC事务的参与者发来的消息
 * 
 * @author liuyg
 */
@Slf4j
@Component
public class TccEventHandler extends EventHandler {

	@Autowired
	private CoordinatorService coordinatorService;
	
	@Override
	public void handle(String payload) {
		try {
			Participant participant = Jacksons.getMapper().readValue(payload, Participant.class);
			coordinatorService.processParticipant(participant);
		} catch (IOException e) {
			log.error("读取JSON报文至实体时发生异常. payload: " + payload + ", entity: Participant.class", e);
			throw new IllegalArgumentException("读取JSON报文至实体时发生异常. payload: " + payload + ", entity: Participant.class");
		}
	}

	@Override
	public String getBusinessType() {
		return Constants.TCC_BUSINESS_TYPE;
	}
}
