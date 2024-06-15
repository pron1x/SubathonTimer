package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.SubTier;
import jakarta.persistence.*;

@Entity
@Table(name = "subscription_event")
public class SubscribeEntity extends EventEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "sub_tier", columnDefinition = "ENUM('PRIME', 'TIER_1', 'TIER_2', 'TIER_3')")
    private SubTier subTier;
}
