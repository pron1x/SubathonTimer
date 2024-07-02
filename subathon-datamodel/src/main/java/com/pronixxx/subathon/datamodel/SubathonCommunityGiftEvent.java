package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.EventType;
import com.pronixxx.subathon.datamodel.enums.SubTier;

public class SubathonCommunityGiftEvent extends SubathonEvent {
    private int amount;
    private SubTier tier;

    public SubathonCommunityGiftEvent() {
        this.setType(EventType.GIFT);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "SubathonCommunityGiftEvent{" +
                "amount=" + amount +
                ", tier=" + tier +
                "} " + super.toString();
    }

    public SubTier getTier() {
        return tier;
    }

    public void setTier(SubTier tier) {
        this.tier = tier;
    }

}
