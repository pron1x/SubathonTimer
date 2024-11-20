package com.pronixxx.subathon.executor;

import com.pronixxx.subathon.util.interfaces.HasLogger;

import java.time.Instant;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Allows the execution of a runnable at a given LocalDateTime (time in UTC). The execution time can be changed
 * after the task has been scheduled, moving the execution time closer or further away.
 */
public class AdjustableScheduledExecutorService implements HasLogger {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> scheduledCommandHandle;

    private volatile Instant executionTime;
    private volatile boolean isTimerPaused;

    public AdjustableScheduledExecutorService() {}

    public synchronized Instant getExecutionTime() {
        return executionTime;
    }

    public synchronized void setExecutionTime(Instant executionTime) {
        this.executionTime = executionTime;
    }

    public synchronized boolean isTimerPaused() {
        return isTimerPaused;
    }

    public synchronized void setTimerPaused(boolean timerPaused) {
        isTimerPaused = timerPaused;
    }


    /**
     * Schedules a Runnable to be executed at the given execution time. The scheduled execution of the runnable can be paused
     * and the scheduled execution time can be adjusted after the runnable has been scheduled.
     * The runnable is only executed once!
     * @param command The runnable to be executed
     * @param executionTime The time at which the command should be executed. Can be adjusted
     */
    public void scheduleCommand(Runnable command, Instant executionTime) {
        this.executionTime = executionTime;
        final Runnable scheduledCommand = () -> {
            if (isExecutionTime()) {
                command.run();
                if(cancelCommand()) {
                    getLogger().trace("Successfully executed command with execution time {} at {}.", executionTime, Instant.now());
                } else {
                    getLogger().warn("command executed at {} but cancelCommand returned '{}'. IsCancelled: {}.", Instant.now(), false, scheduledCommandHandle.isCancelled());
                }
            } else if (isTimerPaused()) {
                getLogger().trace("Execution is paused.");
            } else {
                getLogger().trace("Not executing the command, yet. [Time={}, Execution={}]", Instant.now(), getExecutionTime());
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
                && Instant.now().isAfter(getExecutionTime()));
    }
}
