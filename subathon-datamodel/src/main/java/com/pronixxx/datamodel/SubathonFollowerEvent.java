package com.pronixxx.datamodel;

import com.pronixxx.datamodel.enums.EventType;

public class SubathonFollowerEvent extends SubathonEvent {

    public SubathonFollowerEvent() {
        this.setType(EventType.FOLLOW);
    }
}
