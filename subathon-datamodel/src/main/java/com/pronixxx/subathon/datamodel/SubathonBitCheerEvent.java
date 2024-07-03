package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.EventType;

public class SubathonBitCheerEvent extends SubathonEvent {
    private int amount;

    public SubathonBitCheerEvent() {
        this.setType(EventType.CHEER);
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "SubathonBitEvent{" +
                "amount=" + amount +
                "} " + super.toString();
    }
}
