package com.apm70.bizfuse.tcc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/**
 * TCC 配置类
 * 
 * @author liuyg
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix="bizfuse.tcc.callback.rabbitmq")
public class TccProperties {

	/**
	 * TCC回调消息的队列名
	 */
	private String queue;
	/**
	 * TCC回调消息的路由KEY
	 */
	private String routeKey;
}
