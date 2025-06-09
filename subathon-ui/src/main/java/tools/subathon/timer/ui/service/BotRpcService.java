package tools.subathon.timer.ui.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.rpc.RpcAction;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.datamodel.rpc.RpcStatus;

import java.util.HashMap;
import java.util.Map;

import static tools.subathon.timer.util.GlobalRabbitMQ.CHANNEL_MANAGEMENT_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Service
public class BotRpcService {

    private final RabbitTemplate rabbitTemplate;

    public BotRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean requestChannelJoin(String channelName) {
        RpcResponseEntity<?> response = sendManagementRpcCreateRequest(new HashMap<>(), channelName);
        if (response.getStatusCode() == RpcStatus.OK) {
            return (Boolean) response.getBody();
        } else {
            return false;
        }
    }

    private RpcResponseEntity<?> sendManagementRpcCreateRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.CREATE_OR_UPDATE, CHANNEL_MANAGEMENT_ROUTING_KEY);
    }

    private RpcResponseEntity<?> sendRpcRequest(Map<String, Object> params, Object body, RpcAction action, String routing) {
        RpcRequestEntity<Object> request = new RpcRequestEntity<>();
        request.setAction(action);
        request.setParams(params);
        request.setBody(body);
        return sendRpcToBot(request, routing);
    }

    private RpcResponseEntity<?> sendRpcToBot(RpcRequestEntity<?> request, String route) {
        return (RpcResponseEntity<?>) rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, route, request);
    }
}
