package com.mortisdevelopment.mortismachines.machines.remotecontrol;

import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.remotes.Remote;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class RemoteControlMachine extends Machine {

    private final long pulse;
    private final List<Remote> remotes;
    private final HashMap<String, Remote> remoteById;

    public RemoteControlMachine(String id, List<Structure> structures, long pulse, List<Remote> remotes, HashMap<String, Remote> remoteById) {
        super(id, structures);
        this.pulse = pulse;
        this.remotes = remotes;
        this.remoteById = remoteById;
    }

    public Remote getRemote(String id) {
        for (Remote remote : remotes) {
            if (remote.getId().equalsIgnoreCase(id)) {
                return remote;
            }
        }
        return null;
    }

    public Remote getRemote(ItemStack item) {
        for (Remote remote : remotes) {
            if (remote.isRemote(item)) {
                return remote;
            }
        }
        return null;
    }

    public long getPulse() {
        return pulse;
    }

    public List<Remote> getRemotes() {
        return remotes;
    }

    public HashMap<String, Remote> getRemoteById() {
        return remoteById;
    }
}
