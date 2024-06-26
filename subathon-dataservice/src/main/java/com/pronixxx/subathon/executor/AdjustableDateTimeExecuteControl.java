package com.pronixxx.subathon.executor;

import com.pronixxx.subathon.util.GlobalDefinition;
import com.pronixxx.subathon.util.interfaces.HasLogger;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.*;

/**
 * Allows the execution of a runnable at a given LocalDateTime (time in UTC). The execution time can be changed
 * after the task has been scheduled, moving the execution time closer or further away.
 */
public class AdjustableDateTimeExecuteControl implements HasLogger {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledCommandHandle;

    private volatile LocalDateTime executionTime;
    private volatile boolean isTimerPaused;

    public AdjustableDateTimeExecuteControl(LocalDateTime executionTime, boolean isTimerPaused) {
        this.executionTime = executionTime;
        this.isTimerPaused = isTimerPaused;
    }

    public synchronized LocalDateTime getExecutionTime() {
        return executionTime;
    }

    public synchronized void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }

    public synchronized boolean isTimerPaused() {
        return isTimerPaused;
    }

    public synchronized void setTimerPaused(boolean timerPaused) {
        isTimerPaused = timerPaused;
    }

    /**
     * Schedules a Runnable command to be executed if the set LocalDateTime has passed.
     * This is checked every second.
     * @param command The command to be executed
     */
    public void scheduleCommand(Runnable command) {
        final Runnable scheduledCommand = new Runnable() {
            public void run() {
                if(isExecutionTime()) {
                    command.run();
                } else {
                    getLogger().debug("Not executing the command, yet. [Time={}, Execution={}]", LocalDateTime.now(ZoneId.of(GlobalDefinition.TZ)), getExecutionTime());
                }
            }
        };
        scheduledCommandHandle = scheduler.scheduleAtFixedRate(scheduledCommand, 0, 1, TimeUnit.SECONDS);
    }

    public boolean cancelCommand() {
        return scheduledCommandHandle.cancel(true);
    }

    private synchronized boolean isExecutionTime() {
        if(isTimerPaused) {
            return false;
        }
        return (executionTime != null
                && LocalDateTime.now(ZoneId.of(GlobalDefinition.TZ)).isAfter(getExecutionTime()));
    }
}
