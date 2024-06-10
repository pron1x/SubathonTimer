package com.pronixxx.subathon.seimporter.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @Type(value = StreamElementsSubscribeModel.class, name = "subscriber"),
        @Type(value = StreamElementsFollowModel.class, name = "follow")
})
public class StreamElementsEventModel {
    private LocalDateTime createdAt;
    private String activityId;
    private boolean isMock = false;
    private boolean flagged;
    private String provider;
    private int sessionEventsCount;
    private String channel;
    private String type;
    private LocalDateTime updateAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setIsMock(boolean mock) {
        isMock = mock;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getSessionEventsCount() {
        return sessionEventsCount;
    }

    public void setSessionEventsCount(int sessionEventsCount) {
        this.sessionEventsCount = sessionEventsCount;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getUpdateAt() {
        return updateAt;
    }

    public void setUpdateAt(LocalDateTime updateAt) {
        this.updateAt = updateAt;
    }

    @Override
    public String toString() {
        return "StreamElementsEventModel{" +
                "createdAt=" + createdAt +
                ", activityId='" + activityId + '\'' +
                ", isMock=" + isMock +
                ", flagged=" + flagged +
                ", provider='" + provider + '\'' +
                ", sessionEventsCount=" + sessionEventsCount +
                ", channel='" + channel + '\'' +
                ", type='" + type + '\'' +
                ", updateAt=" + updateAt +
                '}';
    }
}
