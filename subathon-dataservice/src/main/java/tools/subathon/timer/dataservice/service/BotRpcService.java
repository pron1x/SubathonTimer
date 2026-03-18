package tools.subathon.timer.dataservice.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import tools.subathon.rpc.RpcCommand;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.channel.ChannelEventSubscriptionPayload;
import tools.subathon.rpc.payload.channel.CreateChannelEventsSubscriptionPayload;
import tools.subathon.rpc.payload.channel.CreateMessageEventSubscriptionPayload;
import tools.subathon.rpc.payload.response.BatchResponse;

import static tools.subathon.timer.util.GlobalRabbitMQ.CHANNEL_MANAGEMENT_ROUTING_KEY;
import static tools.subathon.timer.util.GlobalRabbitMQ.EXCHANGE_NAME;

@Service
public class BotRpcService {

    private final RabbitTemplate rabbitTemplate;

    public BotRpcService(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public BatchResponse requestMessageEventSubscription(String channelId) {
        RpcRequest<CreateMessageEventSubscriptionPayload> request = new RpcRequest<>();
        CreateMessageEventSubscriptionPayload payload = new CreateMessageEventSubscriptionPayload(channelId);
        request.setPayload(payload);
        request.setCommand(RpcCommand.SUBSCRIBE_CHANNEL_MESSAGES);
        RpcResponse<BatchResponse> response = sendChannelRpcRequest(request);
        if (response instanceof RpcResponse.Success<BatchResponse>(BatchResponse body)) {
            return body;
        } else if (response instanceof RpcResponse.Failure<BatchResponse>) {
            throw new RuntimeException("Error trying to subscribe to event messages!");
        } else {
            throw new RuntimeException("Unexpected response type, this should not happen!");
        }
    }

    public BatchResponse requestAllEventSubscriptions(String broadcasterUserId) {
        RpcRequest<CreateChannelEventsSubscriptionPayload> request = new RpcRequest<>();
        CreateChannelEventsSubscriptionPayload payload = new CreateChannelEventsSubscriptionPayload(broadcasterUserId);
        request.setPayload(payload);
        request.setCommand(RpcCommand.SUBSCRIBE_CHANNEL_EVENTS);
        RpcResponse<BatchResponse> response = sendChannelRpcRequest(request);
        if (response instanceof RpcResponse.Success<BatchResponse>(BatchResponse body)) {
            return body;
        } else if (response instanceof RpcResponse.Failure<BatchResponse>) {
            throw new RuntimeException("Error trying to subscribe to all messages!");
        } else {
            throw new RuntimeException("Unexpected response type, this should not happen!");
        }
    }

    private RpcResponse<BatchResponse> sendChannelRpcRequest(RpcRequest<? extends ChannelEventSubscriptionPayload> request) {
        RpcResponse<BatchResponse> response = rabbitTemplate.convertSendAndReceiveAsType(EXCHANGE_NAME, CHANNEL_MANAGEMENT_ROUTING_KEY, request,
                new ParameterizedTypeReference<>() {});
        if(response == null) {
            return RpcResponse.timeout();
        }
        return response;
    }
}
