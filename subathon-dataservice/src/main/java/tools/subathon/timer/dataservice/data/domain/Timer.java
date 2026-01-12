package tools.subathon.timer.dataservice.data.domain;

import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.TimerEvent;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Duration;
import java.time.Instant;

public class Timer {

    private Long id;
    private final String channelName;
    private final String channelId;
    private Instant startTime;
    private Instant endTime;
    private TimerState state;
    private Instant updateTime;

    private Timer(String channelId, String channelName) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.state = TimerState.UNINITIALIZED;
    }

    public static Timer initialize(String channelId, String channelName, Instant now) {
        Timer timer = new Timer(channelId, channelName);
        timer.state = TimerState.INITIALIZED;
        timer.updateTime = now;

        return timer;
    }

    public TimerEvent start(Duration startTime) {
        if(this.state != TimerState.INITIALIZED) {
            throw new IllegalStateException("Can not start a timer that is not initialized");
        }
        Instant now = Instant.now();
        this.state = TimerState.TICKING;

        this.startTime = now;
        this.endTime = now.plus(startTime);
        this.updateTime = now;
        return createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.INITIALIZED, TimerState.TICKING, null, this.endTime);
    }

    public TimerEvent stop() {
        if(this.state != TimerState.TICKING) {
            throw new IllegalStateException("Can not stop a timer that is not ticking");
        }
        Instant now = Instant.now();
        this.state = TimerState.ENDED;
        this.endTime = now;
        this.updateTime = now;

        return createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.TICKING, TimerState.ENDED, this.endTime, this.endTime);
    }

    public TimerEvent pause() {
        if(this.state != TimerState.TICKING) {
            throw new IllegalStateException("Can not pause a timer that is not ticking");
        }
        Instant now  = Instant.now();
        this.state = TimerState.PAUSED;
        this.updateTime = now;

        return createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.TICKING, TimerState.PAUSED, this.endTime, this.endTime);
    }

    public TimerEvent resume() {
        if(this.state != TimerState.PAUSED) {
            throw new IllegalStateException("Can not resume a timer that is not paused");
        }
        Instant now = Instant.now();
        Duration pausedDuration = Duration.between(this.updateTime, now);

        Instant oldEndTime = this.endTime;

        this.state = TimerState.TICKING;
        this.endTime = oldEndTime.plus(pausedDuration);
        this.updateTime = now;

        return createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.PAUSED, TimerState.TICKING, oldEndTime, this.endTime);
    }

    public TimerEvent addTime(Duration duration) {
        if(this.state != TimerState.TICKING && this.state != TimerState.PAUSED) {
            throw new IllegalStateException("Can only add time to a ticking or paused timer");
        }
        Instant now = Instant.now();
        // Calc paused time
        Instant oldEndTime = this.endTime;
        if(this.state == TimerState.PAUSED) {
            Duration pausedDuration = Duration.between(this.updateTime, now);
            oldEndTime = oldEndTime.plus(pausedDuration);
        }
        this.endTime = oldEndTime.plus(duration);
        this.updateTime = now;
        return createTimerEvent(TimerEventType.TIME_ADDITION, this.state, this.state, oldEndTime, this.endTime);
    }

    public TimerEvent subtractTime(Duration duration) {
        if(this.state != TimerState.TICKING && this.state != TimerState.PAUSED) {
            throw new IllegalStateException("Can only subtract time from a ticking or paused timer");
        }
        Instant now = Instant.now();
        // Calc paused time
        Instant oldEndTime = this.endTime;
        if(this.state == TimerState.PAUSED) {
            Duration pausedDuration = Duration.between(this.updateTime, now);
            oldEndTime = oldEndTime.plus(pausedDuration);
        }
        Instant newEnd = oldEndTime.minus(duration);
        if(newEnd.isBefore(now)) {
            throw new IllegalStateException("Can not subtract more time than remaining");
        }

        this.endTime = newEnd;
        this.updateTime = now;
        return createTimerEvent(TimerEventType.TIME_SUBTRACTION, this.state, this.state, oldEndTime, this.endTime);
    }

    public boolean isActive() {
        return this.state == TimerState.TICKING || this.state == TimerState.PAUSED;
    }

    public boolean isPaused() {
        return this.state == TimerState.PAUSED;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public TimerDto toDto() {
        TimerDto dto = new TimerDto();
        dto.setId(this.id);
        dto.setChannelId(this.channelId);
        dto.setChannelName(this.channelName);
        dto.setStartTime(this.startTime);
        dto.setEndTime(this.endTime);
        dto.setUpdateTime(this.updateTime);
        dto.setState(this.state);
        return dto;
    }

    public static Timer fromDto(TimerDto dto) {
        Timer timer = new Timer(dto.getChannelId(), dto.getChannelName());
        timer.id = dto.getId();
        timer.startTime = dto.getStartTime();
        timer.endTime = dto.getEndTime();
        timer.updateTime = dto.getUpdateTime();
        timer.state = dto.getState();
        return timer;
    }

    private TimerEvent createTimerEvent(TimerEventType type, TimerState oldState, TimerState newState, Instant oldEnd, Instant newEnd) {
        TimerEvent event = new TimerEvent();
        event.setTimerId(this.id);
        event.setChannelId(this.channelId);
        event.setType(type);
        event.setOldTimerState(oldState);
        event.setCurrentTimerState(newState);
        event.setOldEndTime(oldEnd);
        event.setCurrentEndTime(newEnd);
        event.setTimestamp(this.updateTime);
        return event;
    }
}
