package tools.subathon.timer.bot.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.SubathonEventMessage;

import static tools.subathon.timer.util.GlobalRabbitMQ.DATASERVICE_RPC_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Service
public class DataServiceRpcClient {

    private final RabbitTemplate rabbitTemplate;

    public DataServiceRpcClient(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public Boolean requestTimeChange(SubathonEventMessage command) {
        return (Boolean) sendRpcToDataService(command);
    }

    private Object sendRpcToDataService(Object message) {
        return rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, DATASERVICE_RPC_ROUTING_KEY, message);
    }
}
