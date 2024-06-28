package com.pronixxx.subathon.config;

import com.pronixxx.subathon.MessageReceiver;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String EXCHANGE_NAME = "subathon-exchange";
    public static final String SUBATHON_QUEUE_NAME = "subathon-queue";
    public static final String SUBATHON_ROUTING_KEY = "subathon.event.#";

    public static final String BOT_QUEUE_NAME = "bot-queue";
    public static final String BOT_ROUTING_KEY = "bot.event.#";

    @Bean
    Queue subathonQueue() {
        return new Queue(SUBATHON_QUEUE_NAME, true);
    }

    @Bean
    Queue botQueue() {
        return new Queue(BOT_QUEUE_NAME, true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    Binding subathonBinding(TopicExchange exchange) {
        return BindingBuilder.bind(subathonQueue()).to(exchange).with(SUBATHON_ROUTING_KEY);
    }

    @Bean
    Binding botBinding(TopicExchange exchange) {
        return BindingBuilder.bind(botQueue()).to(exchange).with(BOT_ROUTING_KEY);
    }

    @Bean
    SimpleMessageListenerContainer subathonContainer(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(SUBATHON_QUEUE_NAME);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    SimpleMessageListenerContainer botContainer(ConnectionFactory connectionFactory, MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(BOT_QUEUE_NAME);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(MessageReceiver receiver) {
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
}
