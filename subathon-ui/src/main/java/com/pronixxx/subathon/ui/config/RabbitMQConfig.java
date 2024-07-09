package com.pronixxx.subathon.ui.config;

import com.pronixxx.subathon.ui.service.MessageReceiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.pronixxx.subathon.util.GlobalRabbitMQ.*;

@Configuration
public class RabbitMQConfig {

    @Bean
    Queue timerEventQueue() {
        return new Queue(TIMER_EVENT_QUEUE_NAME, true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    Binding timerEventBinding(TopicExchange exchange) {
        return BindingBuilder.bind(timerEventQueue()).to(exchange).with(TIMER_EVENT_ROUTING_KEY);
    }

    @Bean
    SimpleMessageListenerContainer timerEventContainer(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(TIMER_EVENT_QUEUE_NAME);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(MessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
}
