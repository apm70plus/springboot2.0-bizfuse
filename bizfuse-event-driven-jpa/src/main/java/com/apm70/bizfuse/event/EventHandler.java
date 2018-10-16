package com.apm70.bizfuse.event;

import javax.annotation.PostConstruct;

/**
 * 业务消息处理器基类
 * 
 * @author liuyg
 */
public abstract class EventHandler {

	/**
	 * 消息处理
	 * 
	 * @param payload
	 */
	public abstract void handle(String payload);

	/**
	 * 获取业务标识
	 * 
	 * @return
	 */
	public abstract String getBusinessType();

	/**
	 * 注册到“消息消费中心”，准备接收特定的业务消息
	 */
	@PostConstruct
	public void register() {
		EventDrivenSubscriber.register(this, this.getBusinessType());
	}
}
