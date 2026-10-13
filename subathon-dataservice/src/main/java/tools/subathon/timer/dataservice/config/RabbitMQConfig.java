package tools.subathon.timer.dataservice.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.TWITCH_EVENT_QUEUE;
import static tools.subathon.timer.util.GlobalRabbitMQ.TWITCH_EVENT_ROUTING_KEY;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    @Bean
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    Queue twitchEventQueue() {
        return new Queue(TWITCH_EVENT_QUEUE, true);
    }

    @Bean
    Binding twitchEventBinding(TopicExchange exchange) {
        return BindingBuilder.bind(twitchEventQueue()).to(exchange).with(TWITCH_EVENT_ROUTING_KEY);
    }

    @Bean
    MessageConverter messageConverter() {
        return new JacksonJsonMessageConverter();
    }
}
