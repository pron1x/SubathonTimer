package com.pronixxx.datamodel;

import com.pronixxx.datamodel.enums.EventType;
import com.pronixxx.datamodel.enums.SubTier;

public class SubathonSubEvent extends SubathonEvent {
    private SubTier tier;

    public SubathonSubEvent() {
        this.setType(EventType.SUBSCRIPTION);
    }

    public SubTier getTier() {
        return tier;
    }

    public void setTier(SubTier tier) {
        this.tier = tier;
    }
}
