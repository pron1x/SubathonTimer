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
    private LocalDateTime eventTimestamp;
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

    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
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
}
