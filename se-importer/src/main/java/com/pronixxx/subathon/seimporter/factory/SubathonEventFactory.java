package com.pronixxx.subathon.seimporter.factory;

import com.pronixxx.subathon.datamodel.*;
import com.pronixxx.subathon.datamodel.enums.SubTier;
import com.pronixxx.subathon.seimporter.model.*;

public class SubathonEventFactory {
    private static final String SOURCE = "se-importer";

    public static SubathonEvent convertToSubathonEvent(StreamElementsEventModel event) {
        switch (event.getType()) {
            case "follow" -> {
                SubathonFollowerEvent followerEvent = new SubathonFollowerEvent();
                followerEvent.setTimestamp(event.getCreatedAt());
                followerEvent.setSource(SOURCE);
                followerEvent.setMock(event.isMock());
                followerEvent.setUsername(((StreamElementsFollowModel)event).getData().getDisplayName());
                return followerEvent;
            }
            case "subscriber" -> {
                return convertToSubathonSubEvent(event);
            }
            case "tip" -> {
                return convertToSubathonTipEvent(event);
            }
            case "raid" -> {
                return convertToSubathonRaidEvent(event);
            }
            case "communityGiftPurchase" -> {
                return convertToSubathonCommunityGiftEvent(event);
            }
            case "cheer" -> {
                return convertToSubathonBitEvent(event);
            }
            default -> throw new IllegalStateException("Converting for type '" + event.getType() + "' not yet implemented.");
        }
    }

    private static SubathonSubEvent convertToSubathonSubEvent(StreamElementsEventModel event) {
        SubathonSubEvent subEvent = new SubathonSubEvent();
        subEvent.setTimestamp(event.getCreatedAt());
        subEvent.setSource(SOURCE);
        subEvent.setMock(event.isMock());
        subEvent.setUsername(((StreamElementsSubscribeModel) event).getData().getDisplayName());
        StreamElementsSubscribeModel s = (StreamElementsSubscribeModel) event;
        SubTier tier = parseToSubTier(s.getData().getTier());
        subEvent.setTier(tier);
        subEvent.setGifted(s.getData().isGifted());
        subEvent.setSender(s.getData().getSender());
        return subEvent;
    }

    private static SubathonTipEvent convertToSubathonTipEvent(StreamElementsEventModel event) {
        SubathonTipEvent tipEvent = new SubathonTipEvent();
        tipEvent.setTimestamp(event.getCreatedAt());
        tipEvent.setSource(SOURCE);
        tipEvent.setMock(event.isMock());
        StreamElementsTipModel.TipEventData s = ((StreamElementsTipModel) event).getData();

        tipEvent.setUsername(s.getDisplayName());
        tipEvent.setAmount(s.getAmount());
        tipEvent.setCurrency(s.getCurrency());
        return tipEvent;
    }

    private static SubathonRaidEvent convertToSubathonRaidEvent(StreamElementsEventModel event) {
        SubathonRaidEvent raidEvent = new SubathonRaidEvent();
        raidEvent.setTimestamp(event.getCreatedAt());
        raidEvent.setSource(SOURCE);
        raidEvent.setMock(event.isMock());
        StreamElementsRaidModel.RaidDataModel s = ((StreamElementsRaidModel) event).getData();

        raidEvent.setUsername(s.getDisplayName());
        raidEvent.setAmount(s.getAmount());
        return raidEvent;
    }

    private static SubathonCommunityGiftEvent convertToSubathonCommunityGiftEvent(StreamElementsEventModel event) {
        SubathonCommunityGiftEvent giftEvent = new SubathonCommunityGiftEvent();
        giftEvent.setTimestamp(event.getCreatedAt());
        giftEvent.setSource(SOURCE);
        giftEvent.setMock(event.isMock());
        StreamElementsSubGiftModel.GiftSubEventData s = ((StreamElementsSubGiftModel) event).getData();

        giftEvent.setUsername(s.getDisplayName());
        SubTier tier = parseToSubTier(s.getTier());
        giftEvent.setTier(tier);
        giftEvent.setAmount(s.getAmount());
        return giftEvent;
    }

    private static SubathonBitCheerEvent convertToSubathonBitEvent(StreamElementsEventModel event) {
        SubathonBitCheerEvent bitEvent = new SubathonBitCheerEvent();
        bitEvent.setTimestamp(event.getCreatedAt());
        bitEvent.setSource(SOURCE);
        bitEvent.setMock(event.isMock());
        StreamElementsBitEventModel.BitEventData s = ((StreamElementsBitEventModel) event).getData();

        bitEvent.setUsername(s.getDisplayName());
        bitEvent.setAmount(s.getAmount());
        return bitEvent;
    }

    private static SubTier parseToSubTier(String tier) {
        return switch (tier) {
            case "1000" -> SubTier.TIER_1;
            case "2000" -> SubTier.TIER_2;
            case "3000" -> SubTier.TIER_3;
            case "prime" -> SubTier.PRIME;
            default -> throw new IllegalStateException("Unexpected value: " + tier);
        };
    }
}
