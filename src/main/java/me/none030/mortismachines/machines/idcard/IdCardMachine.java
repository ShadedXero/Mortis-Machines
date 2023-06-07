package me.none030.mortismachines.machines.idcard;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.structures.Structure;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class IdCardMachine extends Machine {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final long pulse;
    private final long defaultTime;
    private final List<ItemStack> blacklist;

    public IdCardMachine(String id, List<Structure> structures, List<ItemStack> blacklist, long pulse, long defaultTime) {
        super(id, structures);
        this.blacklist = blacklist;
        this.pulse = pulse;
        this.defaultTime = defaultTime;
    }

    public void sendPulse(IdCardData data, long pulse) {
        new BukkitRunnable() {
            boolean done;
            @Override
            public void run() {
                if (!done) {
                    data.setTask(this.getTaskId());
                    done = true;
                }
                if (data.isActivated()) {
                    stopRedstoneSignal(data);
                }else {
                    sendRedstoneSignal(data);
                }
            }
        }.runTaskTimer(plugin, 0L, pulse);
    }

    public void activate(IdCardManager idCardManager, IdCardData data) {
        idCardManager.getActivatedMachines().add(data.getCore());
        data.cancelTask();
        if (data.isPulse()) {
            sendPulse(data, pulse);
        }else {
            sendRedstoneSignal(data);
        }
    }

    public void deactivate(IdCardManager idCardManager, IdCardData data) {
        idCardManager.getActivatedMachines().add(data.getCore());
        if (data.isPulse()) {
            long time = data.getTime();
            if (time == 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        data.cancelTask();
                        stopRedstoneSignal(data);
                    }
                }.runTaskLater(plugin, 1L);
            }else {
                new BukkitRunnable() {
                    long seconds;
                    @Override
                    public void run() {
                        seconds += 1;
                        if (seconds > time || data.getTask() == null) {
                            data.cancelTask();
                            stopRedstoneSignal(data);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }
        }else {
            long time = data.getTime();
            if (time == 0) {
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        stopRedstoneSignal(data);
                    }
                }.runTaskLater(plugin, 1L);
            }else {
                new BukkitRunnable() {
                    long seconds;
                    @Override
                    public void run() {
                        seconds += 1;
                        if (seconds > time || !data.isActivated()) {
                            stopRedstoneSignal(data);
                            cancel();
                        }
                    }
                }.runTaskTimer(plugin, 0L, 20L);
            }
        }
    }

    public boolean isInBlacklist(ItemStack item) {
        for (ItemStack blacklisted : blacklist) {
            if (blacklisted.isSimilar(item)) {
                return true;
            }
        }
        return false;
    }

    public long getPulse() {
        return pulse;
    }

    public long getDefaultTime() {
        return defaultTime;
    }

    public List<ItemStack> getBlacklist() {
        return blacklist;
    }
}
