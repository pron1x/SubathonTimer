package tools.subathon.timer.bot.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.util.GlobalRabbitMQ;

@Service
public class RabbitMessageService {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produceMessage(SubathonEventMessage message) {
        rabbitTemplate.convertAndSend(GlobalRabbitMQ.EXCHANGE_NAME, GlobalRabbitMQ.TWITCH_EVENT_ROUTING_KEY, message);
    }
}
