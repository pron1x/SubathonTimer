package com.pronixxx.subathon.seimporter.factory;

import com.pronixxx.subathon.datamodel.SubathonEvent;
import com.pronixxx.subathon.datamodel.SubathonFollowerEvent;
import com.pronixxx.subathon.datamodel.SubathonSubEvent;
import com.pronixxx.subathon.datamodel.enums.SubTier;
import com.pronixxx.subathon.seimporter.model.StreamElementsEventModel;
import com.pronixxx.subathon.seimporter.model.StreamElementsFollowModel;
import com.pronixxx.subathon.seimporter.model.StreamElementsSubscribeModel;

public class SubathonEventFactory {

    public static SubathonEvent convertToSubathonEvent(StreamElementsEventModel event) {
        switch (event.getType()) {
            case "follow":
                SubathonFollowerEvent followerEvent = new SubathonFollowerEvent();
                followerEvent.setEventTimestamp(event.getCreatedAt());
                followerEvent.setSource("se-importer");
                followerEvent.setUsername(((StreamElementsFollowModel)event).getData().getDisplayName());
                return followerEvent;
            case "subscriber":
                return convertToSubathonSubEvent(event);
            default:
                throw new IllegalStateException("Unexpected value: " + event.getType());
        }
    }

    private static SubathonSubEvent convertToSubathonSubEvent(StreamElementsEventModel event) {
        SubathonSubEvent subEvent = new SubathonSubEvent();
        subEvent.setEventTimestamp(event.getCreatedAt());
        subEvent.setSource("se-importer");
        subEvent.setUsername(((StreamElementsSubscribeModel) event).getData().getDisplayName());
        StreamElementsSubscribeModel s = (StreamElementsSubscribeModel) event;
        SubTier tier = "1000".equals(s.getData().getTier()) ? SubTier.TIER_1 :
                        "2000".equals(s.getData().getTier()) ? SubTier.TIER_2 :
                        "3000".equals(s.getData().getTier()) ? SubTier.TIER_3 : SubTier.PRIME;
        subEvent.setTier(tier);
        return subEvent;
    }
}
