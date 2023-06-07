package me.none030.mortismachines.machines.timer;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.structures.Structure;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class TimerMachine extends Machine {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final long pulse;
    private final long defaultTime;
    private final long defaultOutputTime;

    public TimerMachine(String id, List<Structure> structures, long pulse, long defaultTime, long defaultOutputTime) {
        super(id, structures);
        this.pulse = pulse;
        this.defaultTime = defaultTime;
        this.defaultOutputTime = defaultOutputTime;
    }

    public void sendPulse(TimerManager timerManager, TimerData data, long pulse, long outputTicks, boolean infinite) {
        new BukkitRunnable() {
            long ticks;
            boolean done;
            @Override
            public void run() {
                if (!done) {
                    data.setTask(this.getTaskId());
                    done = true;
                }
                ticks += pulse;
                if (data.isActivated()) {
                    stopRedstoneSignal(data);
                }else {
                    sendRedstoneSignal(data);
                }
                if (ticks > outputTicks) {
                    timerManager.getActivatedMachines().remove(data.getCore());
                    if (!infinite) {
                        stopRedstoneSignal(data);
                        cancel();
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, pulse);
    }

    public void sendConstant(TimerManager timerManager, TimerData data, long outputTicks, boolean infinite) {
        if (infinite) {
            if (data.isActivated()) {
                stopRedstoneSignal(data);
            }else {
                sendRedstoneSignal(data);
            }
        }else {
            sendRedstoneSignal(data);
        }
        new BukkitRunnable() {
            long ticks;
            boolean done;
            @Override
            public void run() {
                if (!done) {
                    data.setTask(this.getTaskId());
                    done = true;
                }
                ticks += 20;
                if (ticks > outputTicks) {
                    timerManager.getActivatedMachines().remove(data.getCore());
                    if (!infinite) {
                        stopRedstoneSignal(data);
                    }
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public boolean isTime(TimerData data) {
        TimerTime timerTime = data.getTime();
        if (timerTime.isLocal()) {
            return data.getLocalTime() == LocalDateTime.now().getDayOfMonth() && timerTime.isLocalTime();
        } else {
            return timerTime.isTime(data.getCurrentTime());
        }
    }

    public void activate(TimerManager timerManager, TimerData data, boolean check) {
        if (check) {
            if (!isTime(data)) {
                return;
            }
        }
        data.cancelTask();
        data.setCurrentTime(0);
        data.setLocalTime(LocalDateTime.now().plusDays(1).getDayOfMonth());
        timerManager.getActivatedMachines().add(data.getCore());
        if (data.isPulse()) {
            long outputTime = data.getOutputTime();
            TimerTime timerTime = data.getTime();
            if (timerTime.isLocal()) {
                if (outputTime == -1) {
                    long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), timerTime.getLocalDateTime());
                    sendPulse(timerManager, data, pulse, seconds, true);
                }
            }else {
                long time = timerTime.getTime();
                if (outputTime == -1) {
                    sendPulse(timerManager, data, pulse, time, true);
                }
            }
            if (outputTime == 0) {
                sendPulse(timerManager, data, pulse, 1, false);
            }
            if (outputTime > 0) {
                sendPulse(timerManager, data, pulse, outputTime * 20, false);
            }
        }else {
            long outputTime = data.getOutputTime();
            TimerTime timerTime = data.getTime();
            if (timerTime.isLocal()) {
                if (outputTime == -1) {
                    long seconds = ChronoUnit.SECONDS.between(LocalDateTime.now(), timerTime.getLocalDateTime());
                    sendConstant(timerManager, data, seconds, true);
                }
            }else {
                long time = timerTime.getTime();
                if (outputTime == -1) {
                    sendConstant(timerManager, data, time, true);
                }
            }
            if (outputTime == 0) {
                sendConstant(timerManager, data, 1, false);
            }
            if (outputTime > 0) {
                sendConstant(timerManager, data, outputTime * 20, false);
            }
        }
    }

    public long getPulse() {
        return pulse;
    }

    public long getDefaultTime() {
        return defaultTime;
    }

    public long getDefaultOutputTime() {
        return defaultOutputTime;
    }
}
