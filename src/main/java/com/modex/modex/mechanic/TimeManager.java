package com.modex.modex.mechanic;

public class TimeManager { // ตัวจัดการเวลาในเกม
    private long lastUpdateTime = 0; // เวลาที่ล่าสุด

    private long TICK_INTERVAL = 250_000_000L; // ระยะเวลาต่อ Tick
    private int tickSpeedLevel = 1; // tick speed level

    private int minute = 0; // นาที
    private int hour = 8; // ชั่วโมง
    private int day = 1; // วัน

    private boolean isNewDay = false; // วันใหม่
    private boolean isPaused = false; // หยุดเกม

    public int getTickSpeedLevel() { // ดึงค่า tick speed
        return tickSpeedLevel;
    }

    public void changeTickSpeed(int level) { // เปลี่ยน Tick Speed
        switch (level) {
            case 1: // level 1
                TICK_INTERVAL = 250_000_000L;
                tickSpeedLevel = 1;
                break;
            case 2: // level 2
                TICK_INTERVAL = 25_000_000L;
                tickSpeedLevel = 2;
                break;
            default: // default
                TICK_INTERVAL = 250_000_000L;
                tickSpeedLevel = 1;
        }
    }

    public void update(long now) { // update เวลา
        if (isPaused) return;

        if (lastUpdateTime == 0) { // ถ้าเวลาที่ update ล่าสุดเป็น 0
            lastUpdateTime = now; // เก็บค่าเวลาปัจจุบัน
            return;
        }

        isNewDay = false;

        while (now - lastUpdateTime >= TICK_INTERVAL) { // ส่วนต่างเวลามากกว่า tick
            advanceTime(); // update เวลา
            lastUpdateTime += TICK_INTERVAL; // update เวลาล่าสุด
        }
    }

    public double getSmoothMinute(long now) { // ดึงค่านาทีแบบละเอียด
        if (isPaused || lastUpdateTime == 0) return minute; // ถ้าเกมหยุด หรือ lastUpdateTime เป็น 0

        double progress = (double) (now - lastUpdateTime) / TICK_INTERVAL;
        return minute + progress;
    }

    public double getSmoothHour(long now) { // ดึงค่าชั่วโมงแบบละเอียด
        return hour + (getSmoothMinute(now) / 60.0);
    }

    private void advanceTime() { // update เวลา
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

    public boolean isPaused() { // ดึงค่าเกมหยุด
        return isPaused;
    }

    public void setPaused(boolean paused) { // set เกมหยุด
        this.isPaused = paused;
        if (!paused) {
            lastUpdateTime = 0;
        }
    }

    public String getTimeString() { // เวลา เป็น string
        return String.format("Day %d | %02d:%02d", day, hour, minute);
    }

    public int getHour() { // ดึงค่าชั่วโมง
        return hour;
    }

    public int getMinute() { // ดึงค่านาที
        return minute;
    }

    public int getDay() { // ดึงค่าวัน
        return day;
    }

    public void setTime(int day, int hour, int minute) { // set เวลา
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    public int getTotalHours() { // ดึงค่าชั่วโมงทั้งหมด
        return (this.day * 24) + this.hour;
    }

    public boolean isNewDay() { // วันใหม่
        return isNewDay;
    }

    public boolean isNightTime() { // check กลางคืน
        return hour >= 18 || hour <= 6;
    }

    public long getTickInterval() { // ดึงค่า tick
        return TICK_INTERVAL;
    }
}