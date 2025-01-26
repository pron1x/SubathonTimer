package com.pronixxx.subathon.executor;

import com.pronixxx.subathon.util.interfaces.HasLogger;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Allows the execution of a runnable at a given LocalDateTime (time in UTC). The execution time can be changed
 * after the task has been scheduled, moving the execution time closer or further away.
 */
public class AdjustableScheduledExecutorService implements HasLogger {

    public static class TimerTaskConfig {
        private volatile Instant executionTime;
        private volatile boolean isPaused;

        public TimerTaskConfig(Instant executionTime, boolean isPaused) {
            this.executionTime = executionTime;
            this.isPaused = isPaused;
        }

        public Instant getExecutionTime() {
            return executionTime;
        }

        public void setExecutionTime(Instant executionTime) {
            this.executionTime = executionTime;
        }

        public boolean isPaused() {
            return isPaused;
        }

        public void setPaused(boolean paused) {
            isPaused = paused;
        }
    }

    // TODO: Adjust this to include multiple different scheduled commands with adjustable execution times!
    // Maybe a map/list of objects that hold all the info (execution time, paused, command) -> Needs ID to adjust execution time!

    private final Map<String, TimerTaskConfig> timerConfigs = new ConcurrentHashMap<>();
    private final Map<String, ScheduledFuture<?>> timerFutures = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public Instant getExecutionTime(String id) {
        return timerConfigs.get(id).getExecutionTime();
    }

    public void setExecutionTime(String id, Instant executionTime) {
        timerConfigs.get(id).setExecutionTime(executionTime);
    }

    public boolean isPaused(String id) {
        return timerConfigs.get(id).isPaused();
    }

    public void setPaused(String id, boolean isPaused) {
        timerConfigs.get(id).setPaused(isPaused);
    }

    public AdjustableScheduledExecutorService() {}

    /**
     * Schedules a Runnable to be executed at the given execution time. The scheduled execution of the runnable can be paused
     * and the scheduled execution time can be adjusted after the runnable has been scheduled.
     * The runnable is only executed once!
     * @param command The runnable to be executed
     * @param executionTime The time at which the command should be executed. Can be adjusted
     */
    public void scheduleCommand(String id, Runnable command, Instant executionTime) {
        timerConfigs.put(id, new TimerTaskConfig(executionTime, false));
        final Runnable scheduledCommand = () -> {
            TimerTaskConfig config = timerConfigs.get(id);
            ScheduledFuture<?> future = timerFutures.get(id);
            if (isExecutionTime(config.getExecutionTime(), config.isPaused())) {
                command.run();
                if(cancelCommand(future)) {
                    getLogger().trace("Successfully executed command with execution time {} at {}.", config.getExecutionTime(), Instant.now());
                } else {
                    getLogger().warn("command executed at {} but cancelCommand returned '{}'. IsCancelled: {}.", Instant.now(), false, future.isCancelled());
                }
            } else if (config.isPaused()) {
                getLogger().trace("Execution is paused.");
            } else {
                getLogger().trace("Not executing the command, yet. [Time={}, Execution={}]", Instant.now(), config.getExecutionTime());
            }
        };
        timerFutures.put(id, scheduler.scheduleAtFixedRate(scheduledCommand, 0, 1, TimeUnit.SECONDS));
    }

    public boolean cancelCommand(ScheduledFuture<?> future) {
        if(future != null) {
            return future.cancel(true);
        }
        return false;
    }

    private synchronized boolean isExecutionTime(Instant executionTime, boolean isPaused) {
        if(isPaused) {
            return false;
        }
        return (executionTime != null
                && Instant.now().isAfter(executionTime));
    }
}
