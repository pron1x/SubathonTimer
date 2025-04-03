package tools.subathon.timer.datamodel;

import tools.subathon.timer.datamodel.enums.EventType;

public class SubathonFollowerEvent extends SubathonEvent {

    public SubathonFollowerEvent() {
        this.setType(EventType.FOLLOW);
    }

    @Override
    public String toString() {
        return "SubathonFollowerEvent{} " + super.toString();
    }
}
