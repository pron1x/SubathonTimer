package tools.subathon.timer.dataservice.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import tools.subathon.rpc.RpcCommand;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.streamelements.AuthenticateStreamelementsPayload;
import tools.subathon.rpc.payload.streamelements.StreamelementsPayload;

import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;
import static tools.subathon.timer.util.GlobalRabbitMQ.IMPORTER_MANAGEMENT_ROUTING_KEY;

@Service
public class SeImporterRpcService {

    private final RabbitTemplate rabbitTemplate;

    public SeImporterRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public boolean authenticateWithJwt(String jwt) {
        RpcRequest<AuthenticateStreamelementsPayload> request = new RpcRequest<>();
        AuthenticateStreamelementsPayload payload = new AuthenticateStreamelementsPayload(jwt);
        request.setPayload(payload);
        request.setCommand(RpcCommand.AUTHENTICATE_SE);
        return sendStreamElementsRpcRequest(request).getBody();
    }

    private RpcResponse<Boolean> sendStreamElementsRpcRequest(RpcRequest<? extends StreamelementsPayload> request) {
        return rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE_NAME, IMPORTER_MANAGEMENT_ROUTING_KEY, request,
                new org.springframework.core.ParameterizedTypeReference<>() {});
    }
}
