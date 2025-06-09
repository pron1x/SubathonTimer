package tools.subathon.timer.dataservice.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.timer.datamodel.rpc.RpcAction;
import tools.subathon.timer.datamodel.rpc.RpcRequestEntity;
import tools.subathon.timer.datamodel.rpc.RpcResponseEntity;
import tools.subathon.timer.datamodel.rpc.RpcStatus;

import java.util.HashMap;
import java.util.Map;

import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.IMPORTER_MANAGEMENT_ROUTING_KEY;

@Service
public class SeImporterRpcService {

    private final RabbitTemplate rabbitTemplate;

    public SeImporterRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean authenticateWithJwt(String jwt) {
        return sendManagementRpcCreateRequest(new HashMap<>(), jwt).getStatusCode() == RpcStatus.OK;
    }

    private RpcResponseEntity<?> sendManagementRpcCreateRequest(Map<String, Object> params, Object body) {
        return sendRpcRequest(params, body, RpcAction.CREATE_OR_UPDATE, IMPORTER_MANAGEMENT_ROUTING_KEY);
    }

    private RpcResponseEntity<?> sendRpcRequest(Map<String, Object> params, Object body, RpcAction action, String routing) {
        RpcRequestEntity<Object> request = new RpcRequestEntity<>();
        request.setAction(action);
        request.setParams(params);
        request.setBody(body);
        return sendRpcToDataservice(request, routing);
    }

    private RpcResponseEntity<?> sendRpcToDataservice(RpcRequestEntity<?> request, String routing) {
        return (RpcResponseEntity<?>) rabbitTemplate.convertSendAndReceive(EXCHANGE_NAME, routing, request);
    }
}
