package tools.subathon.timer.ui.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.util.GlobalRabbitMQ;

@Service
public class RabbitMessageService {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMessageService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendRpcToBot(Object message) {
        rabbitTemplate.convertAndSend(GlobalRabbitMQ.EXCHANGE_NAME, GlobalRabbitMQ.BOT_RPC_ROUTING_KEY, message);
    }
}
