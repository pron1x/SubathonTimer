package com.pronixxx.subathon.datamodel;

import com.pronixxx.subathon.datamodel.enums.EventType;
import com.pronixxx.subathon.datamodel.enums.Command;

public class SubathonCommandEvent extends SubathonEvent {

    private Command command;
    private long seconds;

    public SubathonCommandEvent() {
        this.setType(EventType.COMMAND);
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public long getSeconds() {
        return seconds;
    }

    public void setSeconds(long seconds) {
        this.seconds = seconds;
    }

    @Override
    public String toString() {
        return "SubathonCommandEvent{" +
                "command=" + command +
                ", seconds=" + seconds +
                "} " + super.toString();
    }
}
