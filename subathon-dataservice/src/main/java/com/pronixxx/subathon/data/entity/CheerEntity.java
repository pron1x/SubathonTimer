package com.pronixxx.subathon.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "cheer_event")
@PrimaryKeyJoinColumn(name = "event_id")
@DiscriminatorValue(value = "CHEER")
public class CheerEntity extends EventEntity {

    @Column(name = "amount")
    private int amount;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "CheerEntity{" +
                "amount=" + amount +
                "} " + super.toString();
    }
}
