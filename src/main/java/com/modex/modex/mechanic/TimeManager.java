package com.modex.modex.mechanic;

public class TimeManager {
    private long lastUpdateTime = 0;

    private long TICK_INTERVAL = 250_000_000L;
    private int tickSpeedLevel = 1;

    private int minute = 0;
    private int hour = 8;
    private int day = 1;

    private boolean isNewDay = false;
    private boolean isPaused = false;

    public int getTickSpeedLevel() {
        return tickSpeedLevel;
    }
    public void changeTickSpeed(int level) {
        switch (level) {
            case 1:
                TICK_INTERVAL = 250_000_000L;
                tickSpeedLevel = 1;
                break;
            case 2:
                TICK_INTERVAL = 125_000_000L;
                tickSpeedLevel = 2;
                break;
            default:
                TICK_INTERVAL = 250_000_000L;
                tickSpeedLevel = 1;
        }
    }

    public void update(long now) {
        if (isPaused) return;

        if (lastUpdateTime == 0) {
            lastUpdateTime = now;
            return;
        }

        isNewDay = false;

        while (now - lastUpdateTime >= TICK_INTERVAL) {
            advanceTime();
            lastUpdateTime += TICK_INTERVAL;
        }
    }

    public double getSmoothMinute(long now) {
        if (isPaused || lastUpdateTime == 0) return minute;

        double progress = (double)(now - lastUpdateTime) / TICK_INTERVAL;
        return minute + progress;
    }

    public double getSmoothHour(long now) {
        return hour + (getSmoothMinute(now) / 60.0);
    }

    private void advanceTime() {
        minute++;
        if (minute >= 60) {
            minute = 0;
            hour++;
        }
        if (hour >= 24) {
            hour = 0;
            day++;
            isNewDay = true;
        }
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
        if (!paused) {
            lastUpdateTime = 0;
        }
    }
    public boolean isPaused() { return isPaused; }

    public String getTimeString() {
        return String.format("Day %d | %02d:%02d", day, hour, minute);
    }

    public int getHour() { return hour; }
    public int getMinute() { return minute; }
    public int getDay() { return day; }

    public void setTime(int day, int hour, int minute) {
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public int getTotalHours() {
        return (this.day * 24) + this.hour;
    }

    public boolean isNewDay() {
        return isNewDay;
    }

    public boolean isNightTime() {
        return hour >= 18 || hour <= 6;
    }

    public long getTickInterval(){return TICK_INTERVAL;}
}