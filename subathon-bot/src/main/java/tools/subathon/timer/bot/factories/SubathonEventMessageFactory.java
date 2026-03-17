package tools.subathon.timer.bot.factories;

import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.EventSubUserChannelEvent;
import tools.subathon.timer.datamodel.SubathonEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.SubathonFollowerEvent;

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

    public static SubathonFollowerEvent createSubathonFollowerEvent(ChannelFollowEvent event) {
        SubathonFollowerEvent followerEvent = new SubathonFollowerEvent();
        followerEvent.setSource(SOURCE);
        followerEvent.setTimestamp(event.getFollowedAt());
        followerEvent.setUsername(event.getUserName());

        return followerEvent;
    }
}
