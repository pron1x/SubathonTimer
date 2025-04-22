package tools.subathon.timer.ui.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.util.GlobalRabbitMQ;

@Service
public class BotRpcService {

    private final RabbitTemplate rabbitTemplate;

    public BotRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean requestChannelJoin(String channelName) {
        return (Boolean) sendRpcToBot(channelName);
    }

    private Object sendRpcToBot(Object message) {
        return rabbitTemplate.convertSendAndReceive(GlobalRabbitMQ.EXCHANGE_NAME, GlobalRabbitMQ.BOT_RPC_ROUTING_KEY, message);
    }
}
