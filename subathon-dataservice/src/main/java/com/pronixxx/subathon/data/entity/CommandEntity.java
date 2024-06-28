package com.pronixxx.subathon.data.entity;

import com.pronixxx.subathon.datamodel.enums.Command;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "command_event")
@PrimaryKeyJoinColumn(name = "event_id")
@DiscriminatorValue(value = "COMMAND")
public class CommandEntity extends EventEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "command", columnDefinition = "ENUM('START','PAUSE','ADD','REMOVE')")
    private Command command;

    @Column(name = "seconds")
    private long seconds;

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
        return "CommandEntity{" +
                "id=" + id +
                ", insertTime=" + insertTime +
                ", command=" + command +
                ", seconds=" + seconds +
                "} " + super.toString();
    }
}
