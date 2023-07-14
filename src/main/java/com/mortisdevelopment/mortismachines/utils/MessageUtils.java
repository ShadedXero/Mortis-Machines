package com.mortisdevelopment.mortismachines.utils;

import com.mortisdevelopment.mortismachines.machines.timer.TimerTime;
import org.bukkit.ChatColor;

import java.util.Objects;

public class MessageUtils {

    private String message;

    public MessageUtils(String message) {
        this.message = Objects.requireNonNullElse(message, "");
    }

    public void color() {
        setMessage(ChatColor.translateAlternateColorCodes('&', message));
    }

    public void replace(String value, String replacement) {
        setMessage(message.replace(value, replacement));
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public TimerTime getTimerTime() {
        Long time = getSeconds();
        if (time == null || time < 1 || time > 86400) {
            long[] localTime = getLocalTime();
            if (localTime == null) {
                return null;
            }
            return new TimerTime(localTime);
        }
        return new TimerTime(time);
    }

    public Long getSeconds() {
        try {
            if (message.contains("s")) {
                return Long.parseLong(message.replace("s", ""));
            }
            if (message.contains("m")) {
                long time = Long.parseLong(message.replace("m", ""));
                return time * 60;
            }
            if (message.contains("h")) {
                long time = Long.parseLong(message.replace("h", ""));
                return time * 60 * 60;
            }
        } catch (NumberFormatException exp) {
            return null;
        }
        return null;
    }

    public long[] getLocalTime() {
        String[] numbers = message.split(":");
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

    public String getString(long[] values) {
        StringBuilder value = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            long number = values[i];
            if (values.length == (i + 1)) {
                value.append(number);
            }else {
                value.append(number).append(":");
            }
        }
        return value.toString();
    }

    public String getMessage() {
        return message;
    }
}
