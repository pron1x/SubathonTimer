package tools.subathon.timer.dataservice.config;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static tools.subathon.timer.util.GlobalRabbitMQ.BOT_COMMAND_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_RPC_QUEUE;
import static tools.subathon.timer.util.GlobalRabbitMQ.TWITCH_EVENT_QUEUE;
import static tools.subathon.timer.util.GlobalRabbitMQ.TWITCH_EVENT_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.USER_CONFIG_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.USER_CONFIG_RPC_QUEUE;

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
    Queue userConfigRpcQueue() {
        return new Queue(USER_CONFIG_RPC_QUEUE, true);
    }

    @Bean
    Queue timerRpcQueue() {
        return new Queue(TIMER_RPC_QUEUE, true);
    }

    @Bean
    Binding twitchEventBinding(TopicExchange exchange) {
        return BindingBuilder.bind(twitchEventQueue()).to(exchange).with(TWITCH_EVENT_ROUTING_KEY);
    }

    @Bean
    Binding botCommandRpcBinding(TopicExchange exchange) {
        return BindingBuilder.bind(timerRpcQueue()).to(exchange).with(BOT_COMMAND_ROUTING_KEY);
    }

    @Bean
    Binding userConfigRpcBinding(TopicExchange exchange) {
        return BindingBuilder.bind(userConfigRpcQueue()).to(exchange).with(USER_CONFIG_ROUTING_KEY);
    }

    @Bean
    Binding timerRpcBinding(TopicExchange exchange) {
        return BindingBuilder.bind(timerRpcQueue()).to(exchange).with(TIMER_ROUTING_KEY);
    }

    @Bean
    MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
