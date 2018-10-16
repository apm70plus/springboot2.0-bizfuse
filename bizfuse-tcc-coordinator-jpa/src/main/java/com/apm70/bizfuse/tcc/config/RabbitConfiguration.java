package com.apm70.bizfuse.tcc.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.apm70.bizfuse.event.EventDrivenSubscriber;
import com.apm70.bizfuse.event.MqEventDrivenListener;
import com.google.common.collect.ImmutableMap;

/**
 * RabbitMQ 配置
 * 
 * @author liuyg
 */
@Configuration
public class RabbitConfiguration {

    public static final String TCC_QUEUE = "q.tcc.coordinator";
    public static final String TCC_DEAD_QUEUE = "q.tcc.coordinator.dead";
    public static final String TCC_ROUTING_KEY = "t666fb88-4cc2-11e7-9226-0242ac130004";
    public static final String DEAD_TCC_ROUTING_KEY = "d666fb88-4cc2-11e7-9226-0242ac130004";

    @Bean
    public DirectExchange defaultExchange() {
        return new DirectExchange(Constants.TCC_DIRECT_EXCHANGE, true, false);
    }

    @Bean
    public Queue tccQueue() {
        final ImmutableMap<String, Object> args =
                ImmutableMap.of("x-dead-letter-exchange", Constants.TCC_DIRECT_EXCHANGE,
                        "x-dead-letter-routing-key", DEAD_TCC_ROUTING_KEY);
        return new Queue(TCC_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding tccBinding() {
        return BindingBuilder.bind(tccQueue()).to(defaultExchange()).with(TCC_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public Queue tccDeadQueue() {
        return new Queue(TCC_DEAD_QUEUE, true, false, false);
    }

    @Bean
    public Binding tccDeadBinding() {
        return BindingBuilder.bind(tccDeadQueue()).to(defaultExchange()).with(DEAD_TCC_ROUTING_KEY);
    }
    
    @Bean
    public EventDrivenSubscriber eventDrivenSubscriber() {
        return new EventDrivenSubscriber();
    }
    
    @Bean
    public SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, MqEventDrivenListener mqEventDrivenListener) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(TCC_QUEUE); // 配置所有需要监听的消息队列
        container.setMessageListener(mqEventDrivenListener);
        return container;
    }
}
