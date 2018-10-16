package com.apm70.bizfuse.event;

/**
 * 消息路由的工厂方法接口。业务方可以经由精心设计的业务类型，生成回调消息路由（Event内部未预留路由字段）
 * 
 * @author liuyg
 *
 */
public interface MessageRouteFacotry {

	MessageRoute get(String businessType);
}
