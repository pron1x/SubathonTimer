package tools.subathon.timer.dataservice.data.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


class TimerTest {

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneId.of("UTC"));
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
    }

    @Test
    void pausePausesTheTimer() {
        Timer timer = Timer.initialize("channelId", "channelName", fixedClock);
        timer.start(Duration.ofMinutes(30));

        assert(timer.isActive());

        timer.pause();

        assert(timer.isPaused());
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
    }

    @Test
    void isActive() {
    }

    @Test
    void isPaused() {
    }

    @Test
    void toDto() {
    }

    @Test
    void fromDto() {
    }
}