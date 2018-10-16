package com.apm70.bizfuse.tcc.config;

import java.io.IOException;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import com.apm70.bizfuse.event.Event;
import com.apm70.bizfuse.event.EventDrivenSubscriber;
import com.apm70.bizfuse.event.MqEventDrivenListener;
import com.apm70.bizfuse.util.Jacksons;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.google.common.collect.ImmutableMap;

/**
 * TCC服务配置
 * 
 * @author liuyg
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass=true)
public class TccConfig {

    @Autowired
    private TccProperties tccProperties;
	
    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(Constants.TCC_DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public Queue tccQueue() {
        final ImmutableMap<String, Object> args =
                ImmutableMap.of("x-dead-letter-exchange", Constants.TCC_DIRECT_EXCHANGE,
                        "x-dead-letter-routing-key", Constants.DEAD_TCC_ROUTING_KEY);
        return new Queue(tccProperties.getQueue(), true, false, false, args);
    }

    @Bean
    public Binding tccBinding() {
        return BindingBuilder.bind(tccQueue()).to(defaultExchange()).with(tccProperties.getRouteKey());
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue tccDeadQueue() {
        return new Queue(Constants.TCC_DEAD_QUEUE, true, false, false);
    }

    @Bean
    public Binding tccDeadBinding() {
        return BindingBuilder.bind(tccDeadQueue()).to(defaultExchange()).with(Constants.DEAD_TCC_ROUTING_KEY);
    }
    
    @Bean
    public EventDrivenSubscriber eventDrivenSubscriber() {
        return new EventDrivenSubscriber();
    }
    
    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
    		MqEventDrivenListener mqEventDrivenListener) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(tccProperties.getQueue());// 配置所有需要监听的消息队列
        container.setMessageListener(mqEventDrivenListener);
        return container;
    }
    
    @Bean
    public MessageListener tccCallbackListener(EventDrivenSubscriber eventDrivenSubscriber) {
    	    return new MessageListener() {
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
    	    };
    }
}
