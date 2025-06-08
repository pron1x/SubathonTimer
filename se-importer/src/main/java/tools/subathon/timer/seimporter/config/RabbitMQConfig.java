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
import static tools.subathon.timer.util.GlobalRabbitMQ.SEIMPORTER_RPC_QUEUE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.SEIMPORTER_RPC_ROUTING_KEY;

@Configuration
public class RabbitMQConfig {

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    Queue rpcQueue() {
        return new Queue(SEIMPORTER_RPC_QUEUE_NAME, true);
    }

    @Bean
    Binding rpcBinding(TopicExchange exchange) {
        return BindingBuilder.bind(rpcQueue()).to(exchange).with(SEIMPORTER_RPC_ROUTING_KEY);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
