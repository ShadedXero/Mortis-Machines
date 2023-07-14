package com.mortisdevelopment.mortismachines.machines.timer;

import java.time.LocalDateTime;

public class TimerTime {

    private long time;
    private long[] localTime;
    private final boolean local;

    public TimerTime(String value) {
        Long time = getTime(value);
        if (time == null) {
            long[] localTime = getLocalTime(value);
            if (localTime == null) {
                this.time = 0;
                this.local = false;
                return;
            }
            this.localTime = localTime;
            this.local = true;
            return;
        }
        this.time = time;
        this.local = false;
    }

    public Long getTime(String value) {
        if (value == null) {
            return null;
        }
        long time;
        try {
            time = Long.parseLong(value);
        }catch (NumberFormatException exp) {
            return null;
        }
        return time;
    }

    public long[] getLocalTime(String value) {
        String[] numbers = value.split(":");
        if (numbers.length < 2 || numbers.length > 3) {
            return null;
        }
        long[] time = new long[numbers.length];
        try {
            for (int i = 0; i < numbers.length; i++) {
                time[i] = Long.parseLong(numbers[i]);
            }
        }catch (NumberFormatException exp) {
            return null;
        }
        return time;
    }

    public TimerTime(long time) {
        this.time = time;
        this.local = false;
    }

    public TimerTime(long[] localTime) {
        this.localTime = localTime;
        this.local = true;
    }

    public LocalDateTime getLocalDateTime() {
        if (!isLocal()) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime time = null;
        if (localTime.length == 2) {
            time = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), (int) localTime[0], (int) localTime[1]);
        }
        if (localTime.length == 3) {
            time = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), (int) localTime[0], (int) localTime[1], (int) localTime[2]);
        }
        return time;
    }

    public boolean isLocalTime() {
        if (!isLocal()) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime time = null;
        if (localTime.length == 2) {
            time = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), (int) localTime[0], (int) localTime[1]);
        }
        if (localTime.length == 3) {
            time = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), (int) localTime[0], (int) localTime[1], (int) localTime[2]);
        }
        return time != null && time.isBefore(now);
    }

    public boolean isTime(long time) {
        if (isLocal()) {
            return false;
        }
        return time > this.time;
    }

    public long getTime() {
        return time;
    }

    public long[] getLocalTime() {
        return localTime;
    }

    public boolean isLocal() {
        return local;
    }
}
