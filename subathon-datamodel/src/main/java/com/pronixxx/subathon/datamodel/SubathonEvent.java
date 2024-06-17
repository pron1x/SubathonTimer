package com.pronixxx.subathon.datamodel;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pronixxx.subathon.datamodel.enums.EventType;

import java.time.LocalDateTime;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SubathonFollowerEvent.class, name = "FOLLOW"),
        @JsonSubTypes.Type(value = SubathonSubEvent.class, name = "SUBSCRIPTION")
})
public class SubathonEvent {

    private Integer id;
    private LocalDateTime timestamp;
    private boolean isMock;
    private String source;
    private EventType type;
    private String username;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isMock() {
        return isMock;
    }

    public void setMock(boolean mock) {
        isMock = mock;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "SubathonEvent{" +
                "id=" + id +
                ", timestamp=" + timestamp +
                ", isMock=" + isMock +
                ", source='" + source + '\'' +
                ", type=" + type +
                ", username='" + username + '\'' +
                '}';
    }
}
