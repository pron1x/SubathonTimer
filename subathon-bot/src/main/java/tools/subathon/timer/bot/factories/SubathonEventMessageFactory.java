package tools.subathon.timer.bot.factories;

import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.EventSubChannelFromToEvent;
import com.github.twitch4j.eventsub.events.EventSubUserChannelEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.SubathonFollowerEvent;
import tools.subathon.timer.datamodel.SubathonRaidEvent;

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
}
