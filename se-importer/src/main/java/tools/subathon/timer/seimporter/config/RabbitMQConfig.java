package tools.subathon.timer.seimporter.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.IMPORTER_MANAGEMENT_QUEUE;
import static tools.subathon.timer.util.GlobalRabbitMQ.IMPORTER_MANAGEMENT_ROUTING_KEY;

@Configuration
public class RabbitMQConfig {

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    Queue importerManagementRpcQueue() {
        return new Queue(IMPORTER_MANAGEMENT_QUEUE, true);
    }

    @Bean
    Binding importerManagementRpcBinding(TopicExchange exchange) {
        return BindingBuilder.bind(importerManagementRpcQueue()).to(exchange).with(IMPORTER_MANAGEMENT_ROUTING_KEY);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
