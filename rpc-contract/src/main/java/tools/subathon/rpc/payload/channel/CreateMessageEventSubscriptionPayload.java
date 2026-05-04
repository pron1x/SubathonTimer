package tools.subathon.rpc.payload.channel;

import java.util.Optional;

public record CreateMessageEventSubscriptionPayload(String broadcasterUserId,
                                                    Optional<String> donationMessageTemplate,
                                                    Optional<String> donationMessageUser) implements ChannelEventSubscriptionPayload {
}
