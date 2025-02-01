package com.pronixxx.subathon.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tip_event")
@PrimaryKeyJoinColumn(name = "event_id")
@DiscriminatorValue(value = "TIP")
public class TipEntity extends EventEntity {

    @Column(name = "amount")
    private double amount;

    @Column(name = "currency")
    private String currency;

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
        return "TipEntity{" +
                "amount=" + amount +
                ", currency='" + currency + '\'' +
                "} " + super.toString();
    }
}
