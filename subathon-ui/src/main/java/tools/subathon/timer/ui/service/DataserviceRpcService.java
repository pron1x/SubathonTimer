package tools.subathon.timer.ui.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.rpc.RpcAction;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.datamodel.user.TwitchAccount;
import tools.subathon.timer.datamodel.user.UserConfigurationModel;

import java.util.HashMap;
import java.util.Map;

import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.TIMER_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.USER_CONFIG_ROUTING_KEY;

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
        RpcResponseEntity<?> response = sendUserConfigRpcGetRequest(params, null);
        if(response != null && response.getBody() != null) {
            return  mapper.convertValue(response.getBody(),  UserConfigurationModel.class);
        } else {
            return null;
        }
    }

    public void saveUserConfiguration(String userId, UserConfigurationModel userConfigurationModel) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        sendUserConfigRpcCreateRequest(params, userConfigurationModel);
    }

    public boolean initializeTimerForChannel(String userId, String channelName) {
        HashMap<String, Object> params = new HashMap<>();
        TwitchAccount twitchAccount = new TwitchAccount();
        twitchAccount.setUserId(userId);
        twitchAccount.setChannelName(channelName);
        RpcResponseEntity<?> response = sendTimerRpcCreateRequest(params, twitchAccount);
        if(response != null && response.getBody() != null) {
            return mapper.convertValue(response.getBody(), Boolean.class);
        } else {
            return false;
        }
    }

    private RpcResponseEntity<?> sendUserConfigRpcGetRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.GET, USER_CONFIG_ROUTING_KEY);
    }

    private RpcResponseEntity<?> sendUserConfigRpcCreateRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.CREATE_OR_UPDATE, USER_CONFIG_ROUTING_KEY);
    }

    private RpcResponseEntity<?> sendTimerRpcCreateRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.CREATE_OR_UPDATE, TIMER_ROUTING_KEY);
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
