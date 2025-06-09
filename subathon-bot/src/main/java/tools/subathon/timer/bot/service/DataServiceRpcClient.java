package tools.subathon.timer.bot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.SubathonCommandEvent;
import tools.subathon.timer.datamodel.rpc.RpcAction;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static tools.subathon.timer.util.GlobalRabbitMQ.BOT_COMMAND_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Service
public class DataServiceRpcClient {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    public DataServiceRpcClient(RabbitTemplate rabbitTemplate, ObjectMapper mapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
    }

    public Boolean executeBotCommand(String channelId, SubathonCommandEvent command) {
        Map<String, Object> params = new HashMap<>();
        params.put("channelId", channelId);
        RpcResponseEntity<?> response = sendCommandRpcCreateRequest(params, command);
        if(response != null && response.getBody() != null) {
            return mapper.convertValue(response.getBody(), Boolean.class);
        } else {
            return false;
        }
    }

    private RpcResponseEntity<?> sendCommandRpcCreateRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.CREATE_OR_UPDATE, BOT_COMMAND_ROUTING_KEY);
    }

    private RpcResponseEntity<?> sendRpcRequest(Map<String, Object> params, Object body, RpcAction action, String route) {
        RpcRequestEntity<Object> request = new RpcRequestEntity<>();
        request.setAction(action);
        request.setParams(params);
        request.setBody(body);
        return sendRpcToDataservice(request, route);
    }

    private RpcResponseEntity<?> sendRpcToDataservice(RpcRequestEntity<?> request, String route) {
        return (RpcResponseEntity<?>) rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, route, request);
    }
}
