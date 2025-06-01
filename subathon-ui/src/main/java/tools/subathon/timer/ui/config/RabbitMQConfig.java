package tools.subathon.timer.ui.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.subathon.timer.util.interfaces.HasLogger;

import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_EVENT_QUEUE;
import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_EVENT_ROUTING_KEY;

@EnableRabbit
@Configuration
public class RabbitMQConfig implements HasLogger {

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    Queue timerEventQueue() {
        return new Queue(TIMER_EVENT_QUEUE, true);
    }

    @Bean
    Binding timerEventBinding(TopicExchange exchange) {
        return BindingBuilder.bind(timerEventQueue()).to(exchange).with(TIMER_EVENT_ROUTING_KEY);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
