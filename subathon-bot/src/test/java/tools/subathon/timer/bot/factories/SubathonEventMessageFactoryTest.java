package tools.subathon.timer.bot.factories;

import com.github.twitch4j.common.util.TypeConvert;
import com.github.twitch4j.eventsub.events.ChannelBanEvent;
import com.github.twitch4j.eventsub.events.ChannelBitsUseEvent;
import com.github.twitch4j.eventsub.events.ChannelFollowEvent;
import com.github.twitch4j.eventsub.events.ChannelRaidEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscribeEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionGiftEvent;
import com.github.twitch4j.eventsub.events.ChannelSubscriptionMessageEvent;
import org.junit.jupiter.api.Test;
import tools.subathon.timer.datamodel.SubathonBitCheerEvent;
import tools.subathon.timer.datamodel.SubathonCommunityGiftEvent;
import tools.subathon.timer.datamodel.SubathonEventMessage;
import tools.subathon.timer.datamodel.SubathonFollowerEvent;
import tools.subathon.timer.datamodel.SubathonRaidEvent;
import tools.subathon.timer.datamodel.SubathonSubEvent;
import tools.subathon.timer.datamodel.enums.SubTier;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SubathonEventMessageFactoryTest {

    @Test
    void createSubathonEventMessage_fromFollowEvent() {
        ChannelFollowEvent followEvent = TypeConvert.jsonToObject(
                    """
                    {
                        "user_id": "1234",
                        "user_login": "cool_user",
                        "user_name": "Cool_User",
                        "broadcaster_user_id": "1337",
                        "broadcaster_user_login": "cooler_user",
                        "broadcaster_user_name": "Cooler_User",
                        "followed_at": "2020-07-15T18:16:11.17106713Z"
                    }""", ChannelFollowEvent.class);

        SubathonEventMessage message = SubathonEventMessageFactory.createSubathonEventMessage(followEvent);
        assertEquals("1337", message.getChannelId());

        SubathonFollowerEvent event = assertInstanceOf(SubathonFollowerEvent.class, message.getSubathonEvent());

        assertEquals("Cool_User", event.getUsername());
        assertEquals(event.getTimestamp(), Instant.parse("2020-07-15T18:16:11.17106713Z"));
    }

    @Test
    void createSubathonEventMessage_fromBitsEvent() {
        ChannelBitsUseEvent bitsEvent = TypeConvert.jsonToObject(
                """
                        {
                            "user_id": "1234",
                            "user_login": "cool_user",
                            "user_name": "Cool_User",
                            "broadcaster_user_id": "1337",
                            "broadcaster_user_login": "cooler_user",
                            "broadcaster_user_name": "Cooler_User",
                            "bits": 2,
                            "type": "cheer",
                            "power_up": null,
                            "message": {
                              "text": "cheer1 hi cheer1",
                              "fragments": [
                                {
                                  "type": "cheermote",
                                  "text": "cheer1",
                                  "cheermote": {
                                    "prefix": "cheer",
                                    "bits": 1,
                                    "tier": 1
                                  },
                                  "emote": null
                                },
                                {
                                  "type": "text",
                                  "text": " hi ",
                                  "cheermote": null,
                                  "emote": null
                                },
                                {
                                  "type": "cheermote",
                                  "text": "cheer1",
                                  "cheermote": {
                                    "prefix": "cheer",
                                    "bits": 1,
                                    "tier": 1
                                  },
                                  "emote": null
                                }
                              ]
                            }
                          }""", ChannelBitsUseEvent.class);

        SubathonEventMessage message = SubathonEventMessageFactory.createSubathonEventMessage(bitsEvent);
        assertEquals("1337", message.getChannelId());

        SubathonBitCheerEvent event = assertInstanceOf(SubathonBitCheerEvent.class, message.getSubathonEvent());
        assertEquals("Cool_User", event.getUsername());
        assertEquals(2, event.getAmount());
    }

    @Test
    void createSubathonEventMessage_fromSubscriptionEvent() {
        ChannelSubscribeEvent subscribeEvent = TypeConvert.jsonToObject(
                """
                {
                        "user_id": "1234",
                        "user_login": "cool_user",
                        "user_name": "Cool_User",
                        "broadcaster_user_id": "1337",
                        "broadcaster_user_login": "cooler_user",
                        "broadcaster_user_name": "Cooler_User",
                        "tier": "1000",
                        "is_gift": false
                    }""", ChannelSubscribeEvent.class);

        SubathonEventMessage message = SubathonEventMessageFactory.createSubathonEventMessage(subscribeEvent);
        assertEquals("1337", message.getChannelId());

        SubathonSubEvent event = assertInstanceOf(SubathonSubEvent.class, message.getSubathonEvent());
        assertEquals("Cool_User", event.getUsername());
        assertEquals(SubTier.TIER_1, event.getTier());
        assertFalse(event.isGifted());
        assertNull(event.getSender());
    }

    @Test
    void createSubathonEventMessage_fromSubscriptionMessageEvent() {
        ChannelSubscriptionMessageEvent subscribeEvent =  TypeConvert.jsonToObject("""
                        {
                                "user_id": "1234",
                                "user_login": "cool_user",
                                "user_name": "Cool_User",
                                "broadcaster_user_id": "1337",
                                "broadcaster_user_login": "cooler_user",
                                "broadcaster_user_name": "Cooler_User",
                                "tier": "1000",
                                "message": {
                                    "text": "Love the stream! FevziGG",
                                    "emotes": [
                                        {
                                            "begin": 23,
                                            "end": 30,
                                            "id": "302976485"
                                        }
                                    ]
                                },
                                "cumulative_months": 15,
                                "streak_months": 1,
                                "duration_months": 6
                            }""",
                ChannelSubscriptionMessageEvent.class);

        SubathonEventMessage message = SubathonEventMessageFactory.createSubathonEventMessage(subscribeEvent);
        assertEquals("1337", message.getChannelId());

        SubathonSubEvent event = assertInstanceOf(SubathonSubEvent.class, message.getSubathonEvent());
        assertEquals("Cool_User", event.getUsername());
        assertEquals(SubTier.TIER_1, event.getTier());
        assertFalse(event.isGifted());
        assertNull(event.getSender());
    }

    @Test
    void createSubathonEventMessage_fromSubscriptionGiftEvent() {
        ChannelSubscriptionGiftEvent giftEvent = TypeConvert.jsonToObject("""
                {
                        "user_id": "1234",
                        "user_login": "cool_user",
                        "user_name": "Cool_User",
                        "broadcaster_user_id": "1337",
                        "broadcaster_user_login": "cooler_user",
                        "broadcaster_user_name": "Cooler_User",
                        "total": 2,
                        "tier": "1000",
                        "cumulative_total": 284,
                        "is_anonymous": false
                    }""", ChannelSubscriptionGiftEvent.class);

        SubathonEventMessage message = SubathonEventMessageFactory.createSubathonEventMessage(giftEvent);
        assertEquals("1337", message.getChannelId());

        SubathonCommunityGiftEvent event = assertInstanceOf(SubathonCommunityGiftEvent.class, message.getSubathonEvent());
        assertEquals("Cool_User", event.getUsername());
        assertEquals(SubTier.TIER_1, event.getTier());
        assertEquals(2, event.getAmount());
    }

    @Test
    void createSubathonEventMessage_fromRaidEvent() {
        ChannelRaidEvent raidEvent = TypeConvert.jsonToObject("""
                {
                        "from_broadcaster_user_id": "1234",
                        "from_broadcaster_user_login": "cool_user",
                        "from_broadcaster_user_name": "Cool_User",
                        "to_broadcaster_user_id": "1337",
                        "to_broadcaster_user_login": "cooler_user",
                        "to_broadcaster_user_name": "Cooler_User",
                        "viewers": 9001
                    }""", ChannelRaidEvent.class);

        SubathonEventMessage message = SubathonEventMessageFactory.createSubathonEventMessage(raidEvent);
        assertEquals("1337", message.getChannelId());

        SubathonRaidEvent event = assertInstanceOf(SubathonRaidEvent.class, message.getSubathonEvent());
        assertEquals("Cool_User", event.getUsername());
        assertEquals(9001, event.getAmount());
    }

    @Test
    void createSubathonEventMessage_throwsErrorOnUnsupportedEventSubUserChannelEvent() {
        ChannelBanEvent banEvent = TypeConvert.jsonToObject("""
                {
                        "user_id": "1234",
                        "user_login": "cool_user",
                        "user_name": "Cool_User",
                        "broadcaster_user_id": "1337",
                        "broadcaster_user_login": "cooler_user",
                        "broadcaster_user_name": "Cooler_User",
                        "moderator_user_id": "1339",
                        "moderator_user_login": "mod_user",
                        "moderator_user_name": "Mod_User",
                        "reason": "Offensive language",
                        "banned_at": "2020-07-15T18:15:11.17106713Z",
                        "ends_at": "2020-07-15T18:16:11.17106713Z",
                        "is_permanent": false
                    }""", ChannelBanEvent.class);

        assertThrows(IllegalArgumentException.class, () ->
                SubathonEventMessageFactory.createSubathonEventMessage(banEvent));
    }

}