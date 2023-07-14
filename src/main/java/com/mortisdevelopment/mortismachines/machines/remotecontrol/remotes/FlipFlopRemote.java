package com.mortisdevelopment.mortismachines.machines.remotecontrol.remotes;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.RemoteControlData;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.RemoteControlMachine;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class FlipFlopRemote extends Remote{

    private final MortisMachines plugin = MortisMachines.getInstance();

    public FlipFlopRemote(String id, ItemStack remote, int range) {
        super(id, remote, range);
    }

    public void activate(RemoteControlMachine machine, RemoteControlData data) {
        data.cancelTask();
        if (data.isPulse()) {
            machine.stopRedstoneSignal(data);
            if (data.isMachineActivated()) {
                data.setMachineActivated(false);
            }else {
                data.setMachineActivated(true);
                new BukkitRunnable() {
                    boolean done;

                    @Override
                    public void run() {
                        if (!done) {
                            data.setTask(this.getTaskId());
                            done = true;
                        }
                        if (data.isActivated()) {
                            machine.stopRedstoneSignal(data);
                        } else {
                            machine.sendRedstoneSignal(data);
                        }
                    }
                }.runTaskTimer(plugin, 0L, machine.getPulse());
            }
        }else {
            if (data.isActivated()) {
                machine.stopRedstoneSignal(data);
            } else {
                machine.sendRedstoneSignal(data);
            }
        }
    }
}
