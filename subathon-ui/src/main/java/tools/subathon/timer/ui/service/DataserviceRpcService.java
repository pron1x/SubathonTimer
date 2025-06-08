package tools.subathon.timer.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.rpc.RpcAction;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;

import java.util.HashMap;
import java.util.Map;

import static tools.subathon.timer.util.GlobalRabbitMQ.DATASERVICE_RPC_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Service
public class DataserviceRpcService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper mapper;

    public DataserviceRpcService(RabbitTemplate rabbitTemplate, ObjectMapper mapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.mapper = mapper;
    }

    public UserConfigurationModel getUserConfiguration(String userId) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        RpcResponseEntity<?> response = sendRpcGetRequest(params, null);
        if(response != null && response.getBody() != null) {
            return  mapper.convertValue(response.getBody(),  UserConfigurationModel.class);
        } else {
            return null;
        }
    }

    public void saveUserConfiguration(String userId, UserConfigurationModel userConfigurationModel) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        sendRpcCreateRequest(params, userConfigurationModel);
    }

    private RpcResponseEntity<?> sendRpcGetRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.GET);
    }

    private RpcResponseEntity<?> sendRpcCreateRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.CREATE_OR_UPDATE);
    }

    private RpcResponseEntity<?> sendRpcDeleteRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.DELETE);
    }

    private RpcResponseEntity<?> sendRpcRequest(Map<String, Object> params, Object body, RpcAction action) {
        RpcRequestEntity<Object> request = new RpcRequestEntity<>();
        request.setAction(action);
        request.setParams(params);
        request.setBody(body);
        return sendRpcToDataservice(request);
    }

    private RpcResponseEntity<?> sendRpcToDataservice(RpcRequestEntity<?> request) {
        return (RpcResponseEntity<?>) rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, DATASERVICE_RPC_ROUTING_KEY, request);
    }
}
