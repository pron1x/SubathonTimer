package com.pronixxx.subathon.data.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "follow_event")
@PrimaryKeyJoinColumn(name = "event_id")
@DiscriminatorValue(value = "FOLLOW")
public class FollowEntity extends EventEntity {
    @Override
    public String toString() {
        return "FollowEntity{} " + super.toString();
    }
}
