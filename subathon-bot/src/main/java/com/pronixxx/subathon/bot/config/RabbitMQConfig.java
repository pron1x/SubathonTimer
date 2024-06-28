package com.pronixxx.subathon.bot.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.pronixxx.subathon.util.GlobalRabbitMQ.BOT_QUEUE_NAME;
import static com.pronixxx.subathon.util.GlobalRabbitMQ.BOT_ROUTING_KEY;
import static com.pronixxx.subathon.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Configuration
public class RabbitMQConfig {

    @Bean
    Queue queue() {
        return new Queue(BOT_QUEUE_NAME, true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(BOT_ROUTING_KEY);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
