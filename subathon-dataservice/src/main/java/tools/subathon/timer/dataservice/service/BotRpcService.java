package tools.subathon.timer.dataservice.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import tools.subathon.rpc.RpcCommand;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.channel.ChannelPayload;
import tools.subathon.rpc.payload.channel.JoinChannelPayload;

import java.util.List;

import static tools.subathon.timer.util.GlobalRabbitMQ.CHANNEL_MANAGEMENT_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Service
public class BotRpcService {

    private final RabbitTemplate rabbitTemplate;

    public BotRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public String requestChannelJoin(String channelName) {
        RpcRequest<JoinChannelPayload> request = new RpcRequest<>();
        JoinChannelPayload payload = new JoinChannelPayload(channelName);
        request.setPayload(payload);
        request.setCommand(RpcCommand.JOIN_CHANNEL);
        RpcResponse<List<String>> response = sendChannelRpcRequest(request);
        if(response instanceof RpcResponse.Success<List<String>>(List<String> body)) {
            return body.getFirst();
        } else {
            return null;
        }
    }

    private RpcResponse<List<String>> sendChannelRpcRequest(RpcRequest<? extends ChannelPayload> request) {
        RpcResponse<List<String>> response = rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE_NAME, CHANNEL_MANAGEMENT_ROUTING_KEY, request,
                new ParameterizedTypeReference<>() {});
        if(response == null) {
            return RpcResponse.timeout();
        }
        return response;
    }
}
