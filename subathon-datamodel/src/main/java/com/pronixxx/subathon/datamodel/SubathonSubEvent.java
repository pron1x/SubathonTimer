package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.EventType;
import com.pronixxx.subathon.datamodel.enums.SubTier;

public class SubathonSubEvent extends SubathonEvent {
    private SubTier tier;
    private boolean gifted;
    private String sender;

    public SubathonSubEvent() {
        this.setType(EventType.SUBSCRIPTION);
    }

    public SubTier getTier() {
        return tier;
    }

    public void setTier(SubTier tier) {
        this.tier = tier;
    }

    public boolean isGifted() {
        return gifted;
    }

    public void setGifted(boolean gifted) {
        this.gifted = gifted;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    @Override
    public String toString() {
        return "SubathonSubEvent{" +
                "tier=" + tier +
                ", gifted=" + gifted +
                ", sender='" + sender + '\'' +
                "} " + super.toString();
    }
}
