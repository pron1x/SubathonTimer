package tools.subathon.timer.dataservice.service;

import tools.subathon.timer.util.GlobalRabbitMQ;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMessageService {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMessage(Object message) throws AmqpException {
        rabbitTemplate.convertAndSend(GlobalRabbitMQ.EXCHANGE_NAME, GlobalRabbitMQ.TIMER_EVENT_ROUTING_KEY, message);
    }
}
