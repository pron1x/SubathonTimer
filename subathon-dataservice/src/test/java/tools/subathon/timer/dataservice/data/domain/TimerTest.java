package tools.subathon.timer.dataservice.data.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.subathon.timer.datamodel.TimerDto;
import tools.subathon.timer.datamodel.enums.TimerEventType;
import tools.subathon.timer.datamodel.enums.TimerState;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class TimerTest {

    private Clock fixedClock;
    private Clock mockedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
        mockedClock = mock(Clock.class);
    }

    @Test
    void initializeCreatesInitialState() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);

        // Timer should only be in INITIALIZED state after initialization
        assert(!timer.isActive());
        assert(!timer.isPaused());

        // End time should not be set
        assertNull(timer.getEndTime());

        // Channel id should be set
        assertEquals("channelId", timer.getChannelId());
    }

    @Test
    void startSetsCorrectEndTimeAndState() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);

        timer.start(Duration.ofMinutes(30));

        // Timer should be active after starting
        assert(timer.isActive());
        assert(!timer.isPaused());

        // End time should be 30 minutes from fixed clock time
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), timer.getEndTime());

        List<TimerEvent> events = timer.getAndClearPendingEvents();
        assertEquals(2, events.size());

        TimerEvent startEvent = events.getLast();

        // Assert that the start event is correctly populated
        assertEquals("channelId", startEvent.getChannelId());
        assertEquals(TimerEventType.STATE_CHANGE, startEvent.getType());

        assertEquals(TimerState.INITIALIZED, startEvent.getOldTimerState());
        assertEquals(TimerState.TICKING, startEvent.getCurrentTimerState());

        assertNull(startEvent.getOldEndTime());
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), startEvent.getCurrentEndTime());

        assertEquals(fixedClock.instant(), startEvent.getTimestamp());
    }

    @Test
    void startFailsOnAnythingButInitialized() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);
        timer.start(Duration.ofMinutes(30));

        // Timer is now ticking, starting again should throw exception
        assertThrows(IllegalStateException.class, () -> timer.start(Duration.ofMinutes(15)));

        // Pause time to change into PAUSED state, should throw when trying to start
        timer.pause();
        assertThrows(IllegalStateException.class, () -> timer.start(Duration.ofMinutes(15)));

        // Start and stop timer to change into ENDED state, should throw when trying to start
        timer.resume();
        timer.stop();
        assertThrows(IllegalStateException.class, () -> timer.start(Duration.ofMinutes(15)));
    }

    @Test
    void stopEndsTheTimerIfTicking() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);
        timer.start(Duration.ofMinutes(30));

        timer.stop();

        // Timer should not be active after stopping
        assert(!timer.isActive());
        assert(!timer.isPaused());

        // End time should be set to the current time, as we can stop 'prematurely' as well
        assertEquals(fixedClock.instant(), timer.getEndTime());

        List<TimerEvent> events = timer.getAndClearPendingEvents();
        assertEquals(3, events.size());

        TimerEvent stopEvent = events.getLast();

        // Assert that the stop event is correctly populated
        assertEquals("channelId", stopEvent.getChannelId());
        assertEquals(TimerEventType.STATE_CHANGE, stopEvent.getType());

        assertEquals(TimerState.TICKING, stopEvent.getOldTimerState());
        assertEquals(TimerState.ENDED, stopEvent.getCurrentTimerState());

        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), stopEvent.getOldEndTime());
        assertEquals(fixedClock.instant(), stopEvent.getCurrentEndTime());

        assertEquals(fixedClock.instant(), stopEvent.getTimestamp());
    }

    @Test
    void stopThrowsExceptionIfNotActive() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);

        // Timer is only initialized, stopping should throw exception
        assertThrows(IllegalStateException.class, timer::stop);

        // Start and then pause the timer
        timer.start(Duration.ofMinutes(30));

        timer.pause();
        assertThrows(IllegalStateException.class, timer::stop);

        // Resume and then stop the timer
        timer.resume();
        timer.stop();

        // Timer is ended, stopping again should throw exception
        assertThrows(IllegalStateException.class, timer::stop);
    }

    @Test
    void pausePausesTheTimer() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);
        timer.start(Duration.ofMinutes(30));

        assert(timer.isActive());

        timer.pause();

        assert(timer.isPaused());

        List<TimerEvent> events = timer.getAndClearPendingEvents();
        assertEquals(3, events.size());

        TimerEvent pauseEvent = events.getLast();

        assertEquals("channelId", pauseEvent.getChannelId());
        assertEquals(TimerEventType.STATE_CHANGE, pauseEvent.getType());

        assertEquals(TimerState.TICKING, pauseEvent.getOldTimerState());
        assertEquals(TimerState.PAUSED, pauseEvent.getCurrentTimerState());

        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), pauseEvent.getOldEndTime());
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), pauseEvent.getCurrentEndTime());

        assertEquals(fixedClock.instant(), pauseEvent.getTimestamp());
    }

    @Test
    void pauseThrowsExceptionIfNotTicking() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);

        // Timer is only initialized, pausing should throw exception
        assertThrows(IllegalStateException.class, timer::pause);

        // Start and then pause the timer
        timer.start(Duration.ofMinutes(30));
        timer.pause();

        // Timer is paused, pausing again should throw exception
        assertThrows(IllegalStateException.class, timer::pause);
    }

    @Test
    void resumeThrowsExceptionIfNotPaused() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);

        // Timer is only initialized, resuming should throw exception
        assertThrows(IllegalStateException.class, timer::resume);

        // Timer is ticking, not paused, resuming should throw exception
        timer.start(Duration.ofMinutes(30));

        assertThrows(IllegalStateException.class, timer::resume);
    }

    @Test
    void resume() {
        Instant init = fixedClock.instant();
        Instant start = fixedClock.instant();
        Instant pause = start.plus(Duration.ofMinutes(10));
        Instant resume = pause.plus(Duration.ofMinutes(5));

        when(mockedClock.instant()).thenReturn(init, start, pause, resume);

        Timer timer = Timer.initialize("channelId", "channelName", mockedClock);
        timer.start(Duration.ofMinutes(30));
        // At this point, end time should be start + 30 minutes
        // Pause after 10 minutes
        timer.pause();
        // 5 Minutes pass while paused
        timer.resume();
        // End time should now be extended by the 5 minutes
        assertEquals(start.plus(Duration.ofMinutes(35)), timer.getEndTime());

        List<TimerEvent> events = timer.getAndClearPendingEvents();
        assertEquals(4, events.size());

        TimerEvent resumeEvent = events.getLast();

        assertEquals("channelId", resumeEvent.getChannelId());
        assertEquals(TimerEventType.STATE_CHANGE, resumeEvent.getType());

        assertEquals(TimerState.PAUSED, resumeEvent.getOldTimerState());
        assertEquals(TimerState.TICKING, resumeEvent.getCurrentTimerState());

        assertEquals(start.plus(Duration.ofMinutes(30)), resumeEvent.getOldEndTime());
        assertEquals(start.plus(Duration.ofMinutes(35)), resumeEvent.getCurrentEndTime());

        assertEquals(resume, resumeEvent.getTimestamp());
    }

    @Test
    void addTimeAddsCorrectDurationToTickingTimer() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);
        timer.start(Duration.ofMinutes(30));

        timer.addTime(Duration.ofMinutes(30));

        // End time should be extended by 30 minutes, from the 30-minute start time
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(60)), timer.getEndTime());

        // Timer state should be unchanged
        assert(timer.isActive());
        assert(!timer.isPaused());

        List<TimerEvent> events = timer.getAndClearPendingEvents();

        assertEquals(3, events.size());
        TimerEvent addEvent = events.getLast();

        assertEquals("channelId", addEvent.getChannelId());
        assertEquals(TimerEventType.TIME_ADDITION, addEvent.getType());

        assertEquals(TimerState.TICKING, addEvent.getOldTimerState());
        assertEquals(TimerState.TICKING, addEvent.getCurrentTimerState());

        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), addEvent.getOldEndTime());
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(60)), addEvent.getCurrentEndTime());

        assertEquals(fixedClock.instant(), addEvent.getTimestamp());
    }

    @Test
    void subtractTimeSubtractsCorrectDurationFromTickingTimer() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);
        timer.start(Duration.ofMinutes(30));

        timer.subtractTime(Duration.ofMinutes(15));

        // End time should be reduced by 15 minutes, from the 30-minute start time
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(15)), timer.getEndTime());

        // Timer state should be unchanged
        assert(timer.isActive());
        assert(!timer.isPaused());

        List<TimerEvent> events = timer.getAndClearPendingEvents();

        assertEquals(3, events.size());

        TimerEvent subtractEvent = events.getLast();

        assertEquals("channelId", subtractEvent.getChannelId());
        assertEquals(TimerEventType.TIME_SUBTRACTION, subtractEvent.getType());

        assertEquals(TimerState.TICKING, subtractEvent.getOldTimerState());
        assertEquals(TimerState.TICKING, subtractEvent.getCurrentTimerState());

        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), subtractEvent.getOldEndTime());
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(15)), subtractEvent.getCurrentEndTime());

        assertEquals(fixedClock.instant(), subtractEvent.getTimestamp());
    }

    @Test
    void isActive() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);
        // Initialized timer should not be active
        assertFalse(timer.isActive());

        // Started time should be active
        timer.start(Duration.ofMinutes(10));
        assertTrue(timer.isActive());

        // Paused timer is active
        timer.pause();
        assertTrue(timer.isActive());

        // Ended timer should not be active
        timer.resume();
        timer.stop();
        assertFalse(timer.isActive());
    }

    @Test
    void isPaused() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);
        // Initialized timer should not be paused
        assertFalse(timer.isPaused());

        // Started time should not be paused
        timer.start(Duration.ofMinutes(10));
        assertFalse(timer.isPaused());

        // Paused timer is paused
        timer.pause();
        assertTrue(timer.isPaused());

        // Resumed timer should not be paused
        timer.resume();
        assertFalse(timer.isPaused());

        // Ended timer should not be paused
        timer.stop();
        assertFalse(timer.isPaused());
    }

    @Test
    void toDto() {
        Instant init = fixedClock.instant();
        Instant start = fixedClock.instant();
        Instant pause = start.plus(Duration.ofMinutes(10));
        Instant resume = pause.plus(Duration.ofMinutes(5));
        when(mockedClock.instant()).thenReturn(init, start, pause, resume);

        Timer timer = Timer.initialize("channelId", "channelName", mockedClock);

        TimerDto dto = timer.toDto();

        assertEquals("channelId", dto.channelId());
        assertEquals("channelName", dto.channelName());
        assertNull(dto.startTime());
        assertNull(dto.endTime());
        assertEquals(init, dto.updateTime());
        assertEquals(TimerState.INITIALIZED, dto.state());

        timer.start(Duration.ofMinutes(30));
        dto = timer.toDto();

        assertEquals("channelId", dto.channelId());
        assertEquals("channelName", dto.channelName());
        assertEquals(start, dto.startTime());
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), dto.endTime());
        assertEquals(start, dto.updateTime());
        assertEquals(TimerState.TICKING, dto.state());

        timer.pause();
        dto = timer.toDto();

        assertEquals("channelId", dto.channelId());
        assertEquals("channelName", dto.channelName());
        assertEquals(start, dto.startTime());
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(30)), dto.endTime());
        assertEquals(pause, dto.updateTime());
        assertEquals(TimerState.PAUSED, dto.state());

        timer.resume();
        dto = timer.toDto();

        assertEquals("channelId", dto.channelId());
        assertEquals("channelName", dto.channelName());
        assertEquals(start, dto.startTime());
        assertEquals(fixedClock.instant().plus(Duration.ofMinutes(35)), dto.endTime());
        assertEquals(resume, dto.updateTime());
        assertEquals(TimerState.TICKING, dto.state());
    }

    @Test
    void fromDto() {
        TimerDto dto = new TimerDto(UUID.randomUUID(),
                "channelId",
                "channelName",
                fixedClock.instant(),
                fixedClock.instant().plus(Duration.ofMinutes(30)),
                TimerState.TICKING,
                fixedClock.instant().plus(Duration.ofMinutes(5)));

        // Convert to timer
        Timer timer = Timer.fromDto(dto);

        // We should get the same dto back when converting back
        TimerDto resultDto = timer.toDto();

        assertEquals(dto.id(), resultDto.id());
        assertEquals(dto.channelId(), resultDto.channelId());
        assertEquals(dto.channelName(), resultDto.channelName());
        assertEquals(dto.startTime(), resultDto.startTime());
        assertEquals(dto.endTime(), resultDto.endTime());
        assertEquals(dto.updateTime(), resultDto.updateTime());
        assertEquals(dto.state(), resultDto.state());
    }
}