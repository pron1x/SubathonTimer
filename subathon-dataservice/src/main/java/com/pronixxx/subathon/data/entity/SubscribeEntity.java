package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.SubTier;
import jakarta.persistence.*;

@Entity
@Table(name = "subscription_event")
@PrimaryKeyJoinColumn(name = "event_id")
@DiscriminatorValue(value = "SUBSCRIPTION")
public class SubscribeEntity extends EventEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", columnDefinition = "ENUM('PRIME', 'TIER_1', 'TIER_2', 'TIER_3')")
    private SubTier tier;

    @Column(name = "gifted")
    private boolean gifted;

    @Column(name = "sender")
    private String sender;

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
        return "SubscribeEntity{" +
                "tier=" + tier +
                ", gifted=" + gifted +
                ", sender='" + sender + '\'' +
                "} " + super.toString();
    }
}
