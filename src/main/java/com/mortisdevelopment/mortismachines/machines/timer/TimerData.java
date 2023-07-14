package com.mortisdevelopment.mortismachines.machines.timer;

import com.mortisdevelopment.mortismachines.data.MachineData;
import com.mortisdevelopment.mortismachines.utils.MachineType;
import com.mortisdevelopment.mortismachines.utils.MessageUtils;
import org.bukkit.Location;

public class TimerData extends MachineData {

    private final String pulseKey = "MortisMachinesPulse";
    private final String timeKey = "MortisMachinesTime";
    private final String currentTimeKey = "MortisMachinesCurrentTime";
    private final String outputTimeKey = "MortisMachinesOutputTime";
    private final String localTimeKey = "MortisMachinesLocalTime";

    public TimerData(Location core) {
        super(core, MachineType.TIMER);
    }

    public void create(String structureId, boolean pulse, TimerTime time, long outputTime) {
        create("TIMER_MACHINE", structureId);
        setPulse(pulse);
        setTime(time);
        setOutputTime(outputTime);
    }

    public boolean isPulse() {
        String value = get(pulseKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setPulse(boolean pulse) {
        set(pulseKey, Boolean.toString(pulse));
    }

    public long getLocalTime() {
        String value = get(localTimeKey);
        if (value == null) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public void setLocalTime(long day) {
        set(localTimeKey, Long.toString(day));
    }

    public TimerTime getTime() {
        String value = get(timeKey);
        if (value == null) {
            return null;
        }
        return new TimerTime(value);
    }

    public void setTime(TimerTime timerTime) {
        if (timerTime.isLocal()) {
            long[] localTime = timerTime.getLocalTime();
            MessageUtils utils = new MessageUtils("");
            set(timeKey, utils.getString(localTime));
        }else {
            set(timeKey, Long.toString(timerTime.getTime()));
        }
    }

    public long getOutputTime() {
        String value = get(outputTimeKey);
        if (value == null) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public void setOutputTime(long outputTime) {
        set(outputTimeKey, Long.toString(outputTime));
    }

    public long getCurrentTime() {
        String value = get(currentTimeKey);
        if (value == null) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public void setCurrentTime(long currentTime) {
        set(currentTimeKey, Long.toString(currentTime));
    }
}
