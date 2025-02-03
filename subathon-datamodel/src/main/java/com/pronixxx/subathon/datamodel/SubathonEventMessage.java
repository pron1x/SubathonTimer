package com.pronixxx.subathon.datamodel;

public class SubathonEventMessage {

    private String channelId;

    private SubathonEvent subathonEvent;

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public SubathonEvent getSubathonEvent() {
        return subathonEvent;
    }

    public void setSubathonEvent(SubathonEvent subathonEvent) {
        this.subathonEvent = subathonEvent;
    }

    @Override
    public String toString() {
        return "SubathonEventMessage{" +
                "channelId='" + channelId + '\'' +
                ", subathonEvent=" + subathonEvent +
                '}';
    }
}
