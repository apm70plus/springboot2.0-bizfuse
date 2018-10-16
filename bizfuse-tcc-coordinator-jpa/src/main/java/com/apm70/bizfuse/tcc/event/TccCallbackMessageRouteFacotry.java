package com.apm70.bizfuse.tcc.event;

import org.springframework.stereotype.Component;

import com.apm70.bizfuse.event.MessageRoute;
import com.apm70.bizfuse.event.MessageRouteFacotry;
import com.apm70.bizfuse.tcc.config.Constants;

/**
 * TCC事务回调通知类消息的路由工厂Bean
 * 
 * @author liuyg
 */
@Component
public class TccCallbackMessageRouteFacotry implements MessageRouteFacotry {

	private static final String SEPARATOR = "\\$";
	
	/**
	 * 通过业务类型生成TCC回调通知的MQ消息路由
	 */
	public MessageRoute get(String businessType) {
		String[] values = businessType.split(SEPARATOR);
		if (values.length == 2) {
			return new MessageRoute(Constants.TCC_DIRECT_EXCHANGE, values[1]);
		} else {
			return null;
		}
	}
}
