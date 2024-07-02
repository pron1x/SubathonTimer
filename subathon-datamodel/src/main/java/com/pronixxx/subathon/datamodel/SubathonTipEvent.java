package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.EventType;

public class SubathonTipEvent extends SubathonEvent {
    private double amount;
    private String currency;

    public SubathonTipEvent() {
        this.setType(EventType.TIP);
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "SubathonTipEvent{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                "} " + super.toString();
    }
}
