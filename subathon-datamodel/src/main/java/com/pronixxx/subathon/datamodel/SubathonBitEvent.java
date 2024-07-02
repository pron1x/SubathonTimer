package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.EventType;

public class SubathonBitEvent extends SubathonEvent {
    private int amount;

    public SubathonBitEvent() {
        this.setType(EventType.BITS);
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
