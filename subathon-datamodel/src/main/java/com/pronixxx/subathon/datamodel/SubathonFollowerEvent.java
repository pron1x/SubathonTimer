package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.EventType;

public class SubathonFollowerEvent extends SubathonEvent {

    public SubathonFollowerEvent() {
        this.setType(EventType.FOLLOW);
    }

    @Override
    public String toString() {
        return "SubathonFollowerEvent{} " + super.toString();
    }
}
