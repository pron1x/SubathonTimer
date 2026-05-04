package tools.subathon.timer.bot.service;

import com.github.twitch4j.eventsub.EventSubSubscription;
import com.github.twitch4j.eventsub.subscriptions.SubscriptionTypes;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.subathon.rpc.RpcRequest;
import tools.subathon.rpc.RpcResponse;
import tools.subathon.rpc.payload.channel.ChannelEventSubscriptionPayload;
import tools.subathon.rpc.payload.channel.CreateChannelEventsSubscriptionPayload;
import tools.subathon.rpc.payload.channel.CreateMessageEventSubscriptionPayload;
import tools.subathon.rpc.payload.response.BatchResponse;
import tools.subathon.timer.bot.SubathonBot;
import tools.subathon.timer.util.interfaces.HasLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static tools.subathon.timer.util.GlobalRabbitMQ.CHANNEL_MANAGEMENT_QUEUE;

@Component
public class RpcRequestHandler implements HasLogger {

    private final SubathonBot twitchBot;

    @Autowired
    public RpcRequestHandler(SubathonBot twitchBot) {
        this.twitchBot = twitchBot;
    }

    @RabbitListener(queues = CHANNEL_MANAGEMENT_QUEUE)
    public RpcResponse<BatchResponse> handleChannelManagementRequest(RpcRequest<ChannelEventSubscriptionPayload> request) {
        getLogger().info("Handling channel management request.");
        return switch(request.getCommand()) {
            case null -> RpcResponse.error("Request command is null.");
            case SUBSCRIBE_CHANNEL_MESSAGES -> {
                CreateMessageEventSubscriptionPayload payload = (CreateMessageEventSubscriptionPayload) request.getPayload();
                List<BatchResponse.ItemResult> results = new ArrayList<>();

                if(twitchBot.subscribeToChannelMessages(payload.broadcasterUserId()).isPresent()) {
                    results.add(new BatchResponse.ItemResult("channelMessageSubscription", payload.broadcasterUserId(), BatchResponse.ItemResult.Status.SUCCESS, null));
                } else {
                    results.add(new BatchResponse.ItemResult("channelMessageSubscription", payload.broadcasterUserId(), BatchResponse.ItemResult.Status.FAILED, "Failed to subscribe to channel messages"));
                }
                payload.donationMessageTemplate().ifPresent(template -> {
                    if (payload.donationMessageUser().isEmpty()) {
                        results.add(new BatchResponse.ItemResult("donationMessageSubscription", payload.broadcasterUserId(), BatchResponse.ItemResult.Status.FAILED, "Donation message user is required when donation message template is provided"));
                    } else if (twitchBot.subscribeToDonationMessages(payload.broadcasterUserId(), template, payload.donationMessageUser().get())) {
                            results.add(new BatchResponse.ItemResult("donationMessageSubscription", payload.broadcasterUserId(), BatchResponse.ItemResult.Status.SUCCESS, null));
                    } else {
                        results.add(new BatchResponse.ItemResult("donationMessageSubscription", payload.broadcasterUserId(), BatchResponse.ItemResult.Status.FAILED, "Failed to subscribe to donation messages"));
                    }
                });
                BatchResponse.BatchStatus status = results.stream().allMatch(r -> r.status() == BatchResponse.ItemResult.Status.SUCCESS) ? BatchResponse.BatchStatus.SUCCESS :
                        results.stream().anyMatch(r -> r.status() == BatchResponse.ItemResult.Status.SUCCESS) ? BatchResponse.BatchStatus.PARTIAL_SUCCESS : BatchResponse.BatchStatus.FAILURE;
                yield RpcResponse.ok(new BatchResponse(status, results));
            }
            case GET_MESSAGE_SUBSCRIBED_CHANNELS ->
                RpcResponse.error("Deprecated!");
            case SUBSCRIBE_CHANNEL_EVENTS -> {
                CreateChannelEventsSubscriptionPayload payload = (CreateChannelEventsSubscriptionPayload) request.getPayload();
                yield createAllSubscriptions(payload.broadcasterUserId());
            }
            default -> RpcResponse.error("Request command is not available for this queue.");
        };
    }

    private RpcResponse<BatchResponse> createAllSubscriptions(String broadcasterUserId) {
        List<BatchResponse.ItemResult> itemResults = new ArrayList<>();
        itemResults.add(subscriptionToItemResult(SubscriptionTypes.CHANNEL_FOLLOW_V2.getName(), broadcasterUserId, twitchBot.subscribeToFollowEvents(broadcasterUserId)));
        itemResults.add(subscriptionToItemResult(SubscriptionTypes.CHANNEL_RAID.getName(), broadcasterUserId, twitchBot.subscribeToRaidEvents(broadcasterUserId)));
        itemResults.add(subscriptionToItemResult(SubscriptionTypes.CHANNEL_BITS_USE.getName(), broadcasterUserId, twitchBot.subscribeToBitsEvents(broadcasterUserId)));

        itemResults.add(subscriptionToItemResult(SubscriptionTypes.CHANNEL_SUBSCRIBE.getName(), broadcasterUserId, twitchBot.subscribeToSubscriptionEvents(broadcasterUserId)));
        itemResults.add(subscriptionToItemResult(SubscriptionTypes.CHANNEL_SUBSCRIPTION_MESSAGE.getName(), broadcasterUserId, twitchBot.subscribeToResubscriptionEvents(broadcasterUserId)));
        itemResults.add(subscriptionToItemResult(SubscriptionTypes.CHANNEL_SUBSCRIPTION_GIFT.getName(), broadcasterUserId, twitchBot.subscribeToSubscriptionGiftEvents(broadcasterUserId)));

        BatchResponse.BatchStatus status = itemResults.stream().allMatch(r -> r.status() == BatchResponse.ItemResult.Status.SUCCESS) ? BatchResponse.BatchStatus.SUCCESS :
            itemResults.stream().anyMatch(r -> r.status() == BatchResponse.ItemResult.Status.SUCCESS) ? BatchResponse.BatchStatus.PARTIAL_SUCCESS : BatchResponse.BatchStatus.FAILURE;

        return RpcResponse.ok(new BatchResponse(status, itemResults));
    }

    private BatchResponse.ItemResult subscriptionToItemResult(String subscriptionName, String broadcasterUserId, Optional<EventSubSubscription> subscription) {
        return new BatchResponse.ItemResult(subscriptionName, broadcasterUserId,
                subscription.isPresent() ? BatchResponse.ItemResult.Status.SUCCESS : BatchResponse.ItemResult.Status.FAILED,
                subscription.isPresent() ? null : "Subscription failed");
    }

}
