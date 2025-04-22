package tools.subathon.timer.dataservice.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static tools.subathon.timer.util.GlobalRabbitMQ.DATASERVICE_EVENT_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EVENT_QUEUE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    @Bean
    Queue eventQueue() {
        return new Queue(EVENT_QUEUE_NAME, true);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    Binding eventBinding(TopicExchange exchange) {
        return BindingBuilder.bind(eventQueue()).to(exchange).with(DATASERVICE_EVENT_ROUTING_KEY);
    }
}
