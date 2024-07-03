package com.pronixxx.subathon.data.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "raid_event")
@PrimaryKeyJoinColumn(name = "event_id")
@DiscriminatorValue(value = "RAID")
public class RaidEntity extends EventEntity {

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
        return "RaidEntity{" +
                "amount=" + amount +
                "} " + super.toString();
    }
}
