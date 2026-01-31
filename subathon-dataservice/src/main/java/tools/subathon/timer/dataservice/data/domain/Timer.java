package tools.subathon.timer.dataservice.data.domain;

import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.dataservice.data.domain.enums.TimerEventType;
import tools.subathon.timer.dataservice.data.domain.enums.TimerState;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Timer {

    private final List<TimerEvent> pendingEvents = new ArrayList<>();

    private final Clock clock;

    private UUID id;
    private final String channelName;
    private final String channelId;
    private Instant startTime;
    private Instant endTime;
    private TimerState state;
    private Instant updateTime;

    private Timer(String channelId, String channelName, Clock clock) {
        this.id = UUID.randomUUID();
        this.clock = clock;
        this.channelId = channelId;
        this.channelName = channelName;
        this.state = TimerState.UNINITIALIZED;
    }

    private Timer(String channelId, String channelName) {
        this(channelId, channelName, Clock.systemUTC());
    }

    public static Timer initialize(String channelId, String channelName, Clock clock) {
        Timer timer = new Timer(channelId, channelName, clock);
        timer.state = TimerState.INITIALIZED;
        timer.updateTime = clock.instant();
        timer.pendingEvents.add(
                timer.createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.UNINITIALIZED, TimerState.INITIALIZED, null, null));

        return timer;
    }

    public static Timer initialize(String channelId, String channelName) {
        return initialize(channelId, channelName, Clock.systemUTC());
    }

    public void start(Duration startTime) {
        if(this.state != TimerState.INITIALIZED) {
            throw new IllegalStateException("Can not start a timer that is not initialized");
        }
        Instant now = clock.instant();
        this.state = TimerState.TICKING;

        this.startTime = now;
        this.endTime = now.plus(startTime);
        this.updateTime = now;
        pendingEvents.add(createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.INITIALIZED, TimerState.TICKING, null, this.endTime));
    }

    public void stop() {
        if(this.state != TimerState.TICKING) {
            throw new IllegalStateException("Can not stop a timer that is not ticking");
        }
        Instant now = clock.instant();
        Instant oldEndTime = this.endTime;

        this.state = TimerState.ENDED;
        this.endTime = now;
        this.updateTime = now;

        pendingEvents.add(createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.TICKING, TimerState.ENDED, oldEndTime, this.endTime));
    }

    public void pause() {
        if(this.state != TimerState.TICKING) {
            throw new IllegalStateException("Can not pause a timer that is not ticking");
        }
        Instant now  = clock.instant();
        this.state = TimerState.PAUSED;
        this.updateTime = now;

        pendingEvents.add(createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.TICKING, TimerState.PAUSED, this.endTime, this.endTime));
    }

    public void resume() {
        if(this.state != TimerState.PAUSED) {
            throw new IllegalStateException("Can not resume a timer that is not paused");
        }
        Instant now = clock.instant();
        Duration pausedDuration = Duration.between(this.updateTime, now);

        Instant oldEndTime = this.endTime;

        this.state = TimerState.TICKING;
        this.endTime = oldEndTime.plus(pausedDuration);
        this.updateTime = now;

        pendingEvents.add(createTimerEvent(TimerEventType.STATE_CHANGE, TimerState.PAUSED, TimerState.TICKING, oldEndTime, this.endTime));
    }

    public void addTime(Duration duration) {
        if(this.state != TimerState.TICKING && this.state != TimerState.PAUSED) {
            throw new IllegalStateException("Can only add time to a ticking or paused timer");
        }
        Instant now = clock.instant();
        // Calc paused time
        Instant oldEndTime = this.endTime;
        if(this.state == TimerState.PAUSED) {
            Duration pausedDuration = Duration.between(this.updateTime, now);
            oldEndTime = oldEndTime.plus(pausedDuration);
        }
        this.endTime = oldEndTime.plus(duration);
        this.updateTime = now;
        pendingEvents.add(createTimerEvent(TimerEventType.TIME_ADDITION, this.state, this.state, oldEndTime, this.endTime));
    }

    public void subtractTime(Duration duration) {
        if(this.state != TimerState.TICKING && this.state != TimerState.PAUSED) {
            throw new IllegalStateException("Can only subtract time from a ticking or paused timer");
        }
        Instant now = clock.instant();
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
        pendingEvents.add(createTimerEvent(TimerEventType.TIME_SUBTRACTION, this.state, this.state, oldEndTime, this.endTime));
    }

    public boolean isActive() {
        return this.state == TimerState.TICKING || this.state == TimerState.PAUSED;
    }

    public boolean isPaused() {
        return this.state == TimerState.PAUSED;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getChannelId() {
        return channelId;
    }

    public Instant getEndTime() {
        return endTime;
    }

    public TimerDto toDto() {
        return new TimerDto(
                this.id,
                this.channelId,
                this.channelName,
                this.startTime,
                this.endTime,
                mapToDtoState(this.state),
                this.updateTime
        );
    }

    public static Timer fromDto(TimerDto dto) {
        Timer timer = new Timer(dto.channelId(), dto.channelName());
        timer.id = dto.id();
        timer.startTime = dto.startTime();
        timer.endTime = dto.endTime();
        timer.updateTime = dto.updateTime();
        timer.state = mapFromDtoState(dto.state());
        return timer;
    }

    private TimerEvent createTimerEvent(TimerEventType type, TimerState oldState, TimerState newState, Instant oldEnd, Instant newEnd) {
        TimerEvent event = new TimerEvent();
        event.setTimerId(this.id);
        event.setChannelId(this.channelId);
        event.setType(mapToDtoType(type));
        event.setOldTimerState(mapToDtoState(oldState));
        event.setCurrentTimerState(mapToDtoState(newState));
        event.setOldEndTime(oldEnd);
        event.setCurrentEndTime(newEnd);
        event.setTimestamp(this.updateTime);
        return event;
    }

    private static tools.subathon.timer.datamodel.enums.TimerState mapToDtoState(TimerState state) {
        return switch (state) {
            case UNINITIALIZED -> tools.subathon.timer.datamodel.enums.TimerState.UNINITIALIZED;
            case INITIALIZED -> tools.subathon.timer.datamodel.enums.TimerState.INITIALIZED;
            case PAUSED -> tools.subathon.timer.datamodel.enums.TimerState.PAUSED;
            case TICKING -> tools.subathon.timer.datamodel.enums.TimerState.TICKING;
            case ENDED -> tools.subathon.timer.datamodel.enums.TimerState.ENDED;
        };
    }

    private static TimerState mapFromDtoState(tools.subathon.timer.datamodel.enums.TimerState state) {
        return switch (state) {
            case UNINITIALIZED -> TimerState.UNINITIALIZED;
            case INITIALIZED -> TimerState.INITIALIZED;
            case PAUSED -> TimerState.PAUSED;
            case TICKING -> TimerState.TICKING;
            case ENDED -> TimerState.ENDED;
        };
    }

    private static tools.subathon.timer.datamodel.enums.TimerEventType mapToDtoType(TimerEventType type) {
        return switch (type) {
            case STATE_CHANGE -> tools.subathon.timer.datamodel.enums.TimerEventType.STATE_CHANGE;
            case TIME_ADDITION -> tools.subathon.timer.datamodel.enums.TimerEventType.TIME_ADDITION;
            case TIME_SUBTRACTION -> tools.subathon.timer.datamodel.enums.TimerEventType.TIME_SUBTRACTION;
        };
    }

    public List<TimerEvent> getAndClearPendingEvents() {
        List<TimerEvent> events = List.copyOf(pendingEvents);
        pendingEvents.clear();
        return events;
    }
}
