package com.example.timereggclock;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class TimerItem {
    private String title;
    private long totalDuration;
    private long timeRemaining;
    private boolean isRunning;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timerHandle;
    private final Runnable updateRunnable;

    public TimerItem(String title, long totalDuration, Runnable updateRunnable) {
        this.title = title;
        this.totalDuration = totalDuration;
        this.timeRemaining = totalDuration;
        this.isRunning = false;
        this.updateRunnable = updateRunnable;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public String getTitle() {
        return title;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void stopTimer() {
        if (isRunning && timerHandle != null) {
            timerHandle.cancel(true);
            timerHandle = null;
            isRunning = false;
            timeRemaining = totalDuration;
            updateRunnable.run();
        }
    }

    public void startTimer() {
        if (!isRunning && timeRemaining > 0) {
            isRunning = true;
            timerHandle = scheduler.scheduleAtFixedRate(() -> {
                if (timeRemaining > 0) {
                    timeRemaining -= 1000;
                    updateRunnable.run();
                } else {
                    stopTimer();
                }
            }, 0, 1000, MILLISECONDS);
        }
    }

    public void pauseTimer() {
        if (isRunning && timerHandle != null) {
            timerHandle.cancel(false);
            isRunning = false;
        }
    }

    public void resetTimer() {
        pauseTimer();
        timeRemaining = totalDuration;
        updateRunnable.run();
    }

    public String getFormattedTimeRemaining() {
        long seconds = (timeRemaining / 1000) % 60;
        long minutes = (timeRemaining / (1000 * 60)) % 60;
        long hours = (timeRemaining / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public void shutDownExecutor() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
