package tools.subathon.timer.dataservice.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.rpc.RpcAction;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.datamodel.rpc.RpcStatus;

import java.util.HashMap;
import java.util.Map;

import static tools.subathon.timer.util.GlobalRabbitMQ.DATASERVICE_RPC_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Service
public class SeImporterRpcService {

    private final RabbitTemplate rabbitTemplate;

    public SeImporterRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean authenticateWithJwt(String jwt) {
        return sendRpcCreateRequest(new HashMap<>(), jwt).getStatusCode() == RpcStatus.OK;
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
