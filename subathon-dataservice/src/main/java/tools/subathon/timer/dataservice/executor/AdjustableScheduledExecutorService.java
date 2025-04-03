package tools.subathon.timer.dataservice.executor;

import tools.subathon.timer.util.interfaces.HasLogger;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Allows the execution of a runnable at a given LocalDateTime (time in UTC). The execution time can be changed
 * after the task has been scheduled, moving the execution time closer or further away.
 */
public class AdjustableScheduledExecutorService implements HasLogger {

    public static class TimerTaskConfig {
        private volatile Instant executionTime;
        private Runnable command;
        private volatile boolean isPaused;

        public TimerTaskConfig(Instant executionTime, Runnable command, boolean isPaused) {
            this.executionTime = executionTime;
            this.command = command;
            this.isPaused = isPaused;
        }

        public Instant getExecutionTime() {
            return executionTime;
        }

        public void setExecutionTime(Instant executionTime) {
            this.executionTime = executionTime;
        }

        public Runnable getCommand() {
            return command;
        }

        public void setCommand(Runnable command) {
            this.command = command;
        }

        public boolean isPaused() {
            return isPaused;
        }

        public void setPaused(boolean paused) {
            isPaused = paused;
        }
    }

    private final Map<String, TimerTaskConfig> timerConfigs;

    private ScheduledFuture<?> interval;

    private final ScheduledExecutorService scheduler;

    public AdjustableScheduledExecutorService() {
        timerConfigs = new ConcurrentHashMap<>();
        scheduler = Executors.newScheduledThreadPool(1);
    }

    public Instant getExecutionTime(String id) {
        synchronized (timerConfigs) {
            if(timerConfigs.get(id) != null) {
                return timerConfigs.get(id).getExecutionTime();
            }
            return null;
        }
    }

    public void setExecutionTime(String id, Instant executionTime) {
        synchronized (timerConfigs) {
            if(timerConfigs.get(id) != null) {
                timerConfigs.get(id).setExecutionTime(executionTime);
            }
        }
    }

    public boolean isPaused(String id) {
        synchronized (timerConfigs) {
            if(timerConfigs.get(id) != null) {
                return timerConfigs.get(id).isPaused();
            }
            return false;
        }
    }

    public void setPaused(String id, boolean isPaused) {
        synchronized (timerConfigs) {
            if(timerConfigs.get(id) != null) {
                timerConfigs.get(id).setPaused(isPaused);
            }
        }
    }

    /**
     * Schedules a Runnable to be executed at the given execution time. The scheduled execution of the runnable can be paused
     * and the scheduled execution time can be adjusted after the runnable has been scheduled.
     * The runnable is only executed once!
     * @param command The runnable to be executed
     * @param executionTime The time at which the command should be executed. Can be adjusted
     */
    public void scheduleCommand(String id, Runnable command, Instant executionTime) {
        synchronized (timerConfigs) {
            if(timerConfigs.get(id) != null) {
                // Perhaps throw an exception here instead of silent returning? Or reschedule the timer with the new runnable
                return;
            }
            timerConfigs.put(id, new TimerTaskConfig(executionTime, command, false));
            // Check if interval exists, create it otherwise!
        }
        if(interval == null || interval.isDone()) {
            interval = scheduler.scheduleAtFixedRate(this::tryExecuteScheduledCommands, 0, 1, TimeUnit.SECONDS);
            getLogger().debug("Started new interval.");
        }
    }

    public boolean cancelCommand(String id) {
        synchronized (timerConfigs) {
            timerConfigs.remove(id);
            return true;
        }
    }

    private void tryExecuteScheduledCommands() {
        List<String> finishedTimers = new ArrayList<>();
        synchronized (timerConfigs) {
            for(Map.Entry<String, TimerTaskConfig> timerEntry : timerConfigs.entrySet()) {
                if(isExecutionTime(timerEntry.getValue().getExecutionTime(), timerEntry.getValue().isPaused())) {
                    try {
                        timerEntry.getValue().getCommand().run();
                    } catch (Exception e) {
                        getLogger().warn("Could not execute command for timer '{}' at {}!", timerEntry.getKey(), timerEntry.getValue().getExecutionTime(), e);
                        continue;
                    }
                    finishedTimers.add(timerEntry.getKey());
                    getLogger().trace("Successfully executed command with id '{}', execution time {} at {}.", timerEntry.getKey(), timerEntry.getValue().getExecutionTime(), Instant.now());
                } else if (timerEntry.getValue().isPaused) {
                    getLogger().trace("Execution is paused for timer with id '{}'.", timerEntry.getKey());
                } else {
                    getLogger().trace("Not executing timer with id '{}' yet.", timerEntry.getKey());
                }
            }
            finishedTimers.forEach(timerConfigs::remove);
            if(timerConfigs.isEmpty()) {
                interval.cancel(true);
                getLogger().debug("Canceled interval because no timers running.");
            }
        }
    }

    private boolean isExecutionTime(Instant executionTime, boolean isPaused) {
        if(isPaused) {
            return false;
        }
        return (executionTime != null
                && Instant.now().isAfter(executionTime));
    }
}
