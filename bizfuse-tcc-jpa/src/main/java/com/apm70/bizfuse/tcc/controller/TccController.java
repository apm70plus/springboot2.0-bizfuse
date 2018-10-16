package com.apm70.bizfuse.tcc.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.apm70.bizfuse.event.EventHandler;
import com.apm70.bizfuse.tcc.config.TccProperties;
import com.apm70.bizfuse.tcc.model.Participant;
import com.apm70.bizfuse.tcc.model.TccStatus;
import com.apm70.bizfuse.util.Jacksons;

import lombok.extern.slf4j.Slf4j;

/**
 * TCC分布式事务 Controller 的基类。通过实现EventHandler，接收TCC事务的回调消息，处理事务成功确认、事务失败补偿的回调逻辑
 * 
 * @author liuyg
 */
@Slf4j
public abstract class TccController extends EventHandler {
	
	private static final String SEPARATOR = "$";
	@Autowired
	protected TccProperties tccProperties;
	
	protected String eventBusinessType;
	
	/**
	 * 执行TCC确认处理
	 * @param participant
	 */
	protected abstract void tccConfirm(Participant participant);
	/**
	 * 执行TCC补偿处理
	 * @param participant
	 */
	protected abstract void tccCancel(Participant participant);

	@Transactional
	public void handle(String payload) {
		try {
			Participant participant = Jacksons.getMapper().readValue(payload, Participant.class);
			if (participant.getTccStatus() == TccStatus.CONFIRMED) {
				this.tccConfirm(participant);
			} else if (participant.getTccStatus() == TccStatus.CANCELED) {
				this.tccCancel(participant);
			} else {
				log.warn("执行TCC回调处理时接收到无效的数据");
			}
		} catch (IOException e) {
			log.error("读取JSON报文至实体时发生异常. payload: " + payload);
		} catch (Exception e) {
			log.error("执行TCC回调处理时发生异常. payload: " + payload, e);
			throw e;
		}
	}

	public String getBusinessType() {
		if (eventBusinessType == null) {
			// businessType 规则 {controllerName}${routingKey}
			eventBusinessType = this.getClass().getSimpleName() + SEPARATOR + tccProperties.getRouteKey();
		}
		return eventBusinessType;
	}
}
