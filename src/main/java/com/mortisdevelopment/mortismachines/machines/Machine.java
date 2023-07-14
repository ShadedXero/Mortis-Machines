package com.mortisdevelopment.mortismachines.machines;

import com.mortisdevelopment.mortismachines.data.MachineData;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import java.util.List;

public abstract class Machine {

    private final BlockData powered = Bukkit.createBlockData("minecraft:lever[face=floor,powered=true]");
    private final BlockData unpowered = Bukkit.createBlockData("minecraft:lever[face=floor,powered=false]");
    private final String id;
    private final List<Structure> structures;

    public Machine(String id, List<Structure> structures) {
        this.id = id;
        this.structures = structures;
    }

    public void sendRedstoneSignal(MachineData machineData) {
        if (machineData.isActivated()) {
            return;
        }
        machineData.setActivated(true);
        Location location = machineData.getCore();
        Block block = location.getBlock();
        block.setBlockData(powered);
    }

    public void stopRedstoneSignal(MachineData machineData) {
        if (!machineData.isActivated()) {
            return;
        }
        machineData.setActivated(false);
        Location location = machineData.getCore();
        Block block = location.getBlock();
        block.setBlockData(unpowered);
    }

    public Structure getStructure(Location location, boolean core) {
        for (Structure structure : structures) {
            if (structure.isStructure(location, core)) {
                return structure;
            }
        }
        return null;
    }

    public Structure getStructure(String structureId) {
        for (Structure structure : structures) {
            if (structure.getId().equalsIgnoreCase(structureId)) {
                return structure;
            }
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public List<Structure> getStructures() {
        return structures;
    }
}
