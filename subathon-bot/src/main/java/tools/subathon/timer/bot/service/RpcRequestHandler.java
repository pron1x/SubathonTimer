package tools.subathon.timer.bot.service;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.channel.ChannelEventSubscriptionPayload;
import tools.subathon.rpc.payload.channel.CreateMessageEventSubscriptionPayload;
import tools.subathon.timer.bot.SubathonBot;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.List;

import static tools.subathon.timer.util.GlobalRabbitMQ.CHANNEL_MANAGEMENT_QUEUE;

@Component
public class RpcRequestHandler implements HasLogger {

    private final SubathonBot twitchBot;

    @Autowired
    public RpcRequestHandler(SubathonBot twitchBot) {
        this.twitchBot = twitchBot;
    }

    @RabbitListener(queues = CHANNEL_MANAGEMENT_QUEUE)
    public RpcResponse<List<String>> handleChannelManagementRequest(RpcRequest<ChannelEventSubscriptionPayload> request) {
        getLogger().info("Handling channel management request.");
        return switch(request.getCommand()) {
            case null -> RpcResponse.error("Request command is null.");
            case SUBSCRIBE_CHANNEL_MESSAGES -> {
                CreateMessageEventSubscriptionPayload payload = (CreateMessageEventSubscriptionPayload) request.getPayload();
                if(twitchBot.subscribeToChannelMessages(payload.channelId())) {
                    yield RpcResponse.ok(List.of(payload.channelId()));
                } else {
                    yield RpcResponse.error("Could not join channel " + payload.channelId());
                }
            }
            case GET_MESSAGE_SUBSCRIBED_CHANNELS ->
                RpcResponse.ok(twitchBot.getMessageSubscriptionChannelIds());
            default -> RpcResponse.error("Request command is not available for this queue.");
        };
    }
}
