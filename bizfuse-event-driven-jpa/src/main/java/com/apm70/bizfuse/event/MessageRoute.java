package com.apm70.bizfuse.event;

import lombok.Value;

/**
 * 消息路由
 * 
 * @author liuyg
 */
@Value
public class MessageRoute {
	/**
	 * MQ的交换机
	 */
    private String exchange;

    /**
     * 消息队列的路由KEY，通过路由KEY，可将消息发布到一个或多个消息队列，或者广播到所有消息队列（视交换机类型而定）
     */
    private String routeKey;
}
