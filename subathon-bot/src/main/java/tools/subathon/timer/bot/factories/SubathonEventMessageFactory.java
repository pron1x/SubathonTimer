package tools.subathon.timer.bot.factories;

import com.github.twitch4j.common.enums.SubscriptionPlan;
import com.github.twitch4j.eventsub.events.ChannelBitsUseEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent;
import com.github.twitch4j.eventsub.events.EventSubChannelFromToEvent;
import com.github.twitch4j.eventsub.events.EventSubUserChannelEvent;
import tools.subathon.timer.datamodel.SubathonBitCheerEvent;
import tools.subathon.timer.datamodel.SubathonCommunityGiftEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.SubathonFollowerEvent;
import tools.subathon.timer.datamodel.SubathonRaidEvent;
import tools.subathon.timer.datamodel.SubathonSubEvent;
import tools.subathon.timer.datamodel.enums.SubTier;

import java.time.Instant;

public class SubathonEventMessageFactory {

    private static final String SOURCE = "twitch-eventsub";

    private SubathonEventMessageFactory() {
        // Constructor hidden
    }

    public static SubathonEventMessage createSubathonEventMessage(EventSubUserChannelEvent event) {
        SubathonEventMessage eventMessage = new SubathonEventMessage();
        eventMessage.setChannelId(event.getBroadcasterUserId());
        SubathonEvent subathonEvent = switch (event) {
            case ChannelFollowEvent followEvent -> createSubathonFollowerEvent(followEvent);
            case ChannelBitsUseEvent bitsEvent -> createSubathonCheerEvent(bitsEvent);
            case ChannelSubscribeEvent subscribeEvent -> createSubathonSubscribeEvent(subscribeEvent);
            case ChannelSubscriptionMessageEvent resubscribeEvent -> createSubathonSubscribeEvent(resubscribeEvent);
            case ChannelSubscriptionGiftEvent subGiftEvent -> createSubathonSubscriptionGiftEvent(subGiftEvent);
            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        };

        eventMessage.setSubathonEvent(subathonEvent);
        return eventMessage;
    }

    public static SubathonEventMessage createSubathonEventMessage(EventSubChannelFromToEvent event) {
        SubathonEventMessage eventMessage = new SubathonEventMessage();
        eventMessage.setChannelId(event.getToBroadcasterUserId());
        SubathonEvent subathonEvent = switch (event) {
            case ChannelRaidEvent raidEvent -> createSubathonRaidEvent(raidEvent);
            default -> throw new IllegalArgumentException("Unsupported event type: " + event.getClass().getName());
        };
        eventMessage.setSubathonEvent(subathonEvent);
        return eventMessage;
    }

    private static SubathonEvent createSubathonRaidEvent(ChannelRaidEvent event) {
        SubathonRaidEvent raidEvent = new SubathonRaidEvent();
        raidEvent.setSource(SOURCE);
        raidEvent.setTimestamp(Instant.now());
        raidEvent.setAmount(event.getViewers());
        raidEvent.setUsername(event.getFromBroadcasterUserName());

        return raidEvent;
    }

    public static SubathonFollowerEvent createSubathonFollowerEvent(ChannelFollowEvent event) {
        SubathonFollowerEvent followerEvent = new SubathonFollowerEvent();
        followerEvent.setSource(SOURCE);
        followerEvent.setTimestamp(event.getFollowedAt());
        followerEvent.setUsername(event.getUserName());

        return followerEvent;
    }

    private static SubathonEvent createSubathonCheerEvent(ChannelBitsUseEvent event) {
        SubathonBitCheerEvent cheerEvent = new SubathonBitCheerEvent();
        cheerEvent.setSource(SOURCE);
        cheerEvent.setTimestamp(Instant.now());
        cheerEvent.setUsername(event.getUserName());
        cheerEvent.setAmount(event.getBits());

        return cheerEvent;
    }

    private static SubathonSubEvent createSubathonSubscribeEvent(ChannelSubscribeEvent event) {
        SubathonSubEvent subEvent = new SubathonSubEvent();
        subEvent.setSource(SOURCE);
        subEvent.setTimestamp(Instant.now());
        subEvent.setUsername(event.getUserName());
        subEvent.setGifted(event.isGift());
        subEvent.setTier(planToTier(event.getTier()));

        return subEvent;
    }

    private static SubathonSubEvent createSubathonSubscribeEvent(ChannelSubscriptionMessageEvent event) {
        SubathonSubEvent subEvent = new SubathonSubEvent();
        subEvent.setSource(SOURCE);
        subEvent.setTimestamp(Instant.now());
        subEvent.setUsername(event.getUserName());
        subEvent.setGifted(false);
        subEvent.setTier(planToTier(event.getTier()));

        return subEvent;
    }

    private static SubathonCommunityGiftEvent createSubathonSubscriptionGiftEvent(ChannelSubscriptionGiftEvent event) {
        SubathonCommunityGiftEvent giftEvent = new SubathonCommunityGiftEvent();
        giftEvent.setSource(SOURCE);
        giftEvent.setTimestamp(Instant.now());
        giftEvent.setUsername(event.getUserName());
        giftEvent.setAmount(event.getTotal());
        giftEvent.setTier(planToTier(event.getTier()));

        return giftEvent;
    }

    private static SubTier planToTier(SubscriptionPlan plan) {
        return switch (plan) {
            case NONE -> throw new IllegalArgumentException("Plan is NONE");
            case TWITCH_PRIME -> SubTier.PRIME;
            case TIER1 -> SubTier.TIER_1;
            case TIER2 -> SubTier.TIER_2;
            case TIER3 -> SubTier.TIER_3;
        };
    }
}
