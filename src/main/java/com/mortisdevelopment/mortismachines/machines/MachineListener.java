package com.mortisdevelopment.mortismachines.machines;

import com.mortisdevelopment.mortismachines.data.MachineData;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MachineListener implements Listener {

    private final Manager manager;

    public MachineListener(Manager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMachineBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (!e.isDropItems()) {
            return;
        }
        if (!player.hasPermission("mortismachines.access")) {
            if (e.isCancelled()) {
                return;
            }
        }else {
            e.setCancelled(false);
        }
        Location loc = e.getBlock().getLocation();
        for (Location location : manager.getCores()) {
            if (location.equals(loc)) {
                MachineData data = new MachineData(location, manager.getType());
                if (!data.isMachine()) {
                    return;
                }
                Machine machine = manager.getMachineById().get(data.getId());
                if (machine == null) {
                    return;
                }
                Structure structure = machine.getStructure(data.getStructureId());
                if (structure == null) {
                    return;
                }
                data.delete();
                manager.delete(location, structure.getCore().getData());
                return;
            }
        }
    }

    @EventHandler
    public void onMachineExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        for (Location location : manager.getCores()) {
            for (Block block : e.blockList()) {
                Location loc = block.getLocation();
                if (location.equals(loc)) {
                    MachineData data = new MachineData(location, manager.getType());
                    if (!data.isMachine()) {
                        return;
                    }
                    Machine machine = manager.getMachineById().get(data.getId());
                    if (machine == null) {
                        return;
                    }
                    Structure structure = machine.getStructure(data.getStructureId());
                    if (structure == null) {
                        return;
                    }
                    data.delete();
                    manager.delete(location, structure.getCore().getData());
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplodeMachine(EntityExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        for (Location location : manager.getCores()) {
            for (Block block : e.blockList()) {
                Location loc = block.getLocation();
                if (location.equals(loc)) {
                    MachineData data = new MachineData(location, manager.getType());
                    if (!data.isMachine()) {
                        return;
                    }
                    Machine machine = manager.getMachineById().get(data.getId());
                    if (machine == null) {
                        return;
                    }
                    Structure structure = machine.getStructure(data.getStructureId());
                    if (structure == null) {
                        return;
                    }
                    data.delete();
                    manager.delete(location, structure.getCore().getData());
                }
            }
        }
    }

    @EventHandler
    public void onMachinePhysics(BlockPhysicsEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Block block = e.getBlock();
        Location loc = block.getLocation();
        for (Location location : manager.getCores()) {
            if (location.equals(loc)) {
                MachineData data = new MachineData(location, manager.getType());
                if (!data.isMachine()) {
                    return;
                }
                Machine machine = manager.getMachineById().get(data.getId());
                if (machine == null) {
                    return;
                }
                Structure structure = machine.getStructure(data.getStructureId());
                if (structure == null) {
                    return;
                }
                if (!block.getRelative(0, -1, 0).getType().equals(Material.AIR)) {
                    return;
                }
                data.delete();
                manager.delete(location, structure.getCore().getData());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        manager.getDataByPlayer().remove(e.getPlayer().getUniqueId());
    }
}
