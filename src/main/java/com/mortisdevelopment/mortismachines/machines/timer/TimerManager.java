package com.mortisdevelopment.mortismachines.machines.timer;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.data.DataManager;
import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.machines.Manager;
import com.mortisdevelopment.mortismachines.menu.MenuItems;
import com.mortisdevelopment.mortismachines.structures.Structure;
import com.mortisdevelopment.mortismachines.utils.MachineType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class TimerManager extends Manager {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final MenuItems menuItems;
    private final HashMap<UUID, TimerData> outputDataByPlayer;

    public TimerManager(DataManager dataManager, MenuItems menuItems) {
        super(MachineType.TIMER, dataManager, true, true);
        this.menuItems = menuItems;
        this.outputDataByPlayer = new HashMap<>();
        plugin.getServer().getPluginManager().registerEvents(new TimerListener(this), plugin);
        check();
    }

    private void check() {
        TimerManager timerManager = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                getPlayersInMenuCoolDown().clear();
                for (int i = 0; i < getCores().size(); i++) {
                    Location core = getCores().get(i);
                    if (core == null) {
                        continue;
                    }
                    TimerData data = new TimerData(core);
                    if (!data.isMachine()) {
                        delete(core);
                        continue;
                    }
                    Machine machine = timerManager.getMachineById().get(data.getId());
                    if (!(machine instanceof TimerMachine)) {
                        data.delete();
                        delete(core);
                        continue;
                    }
                    Structure structure = machine.getStructure(data.getStructureId());
                    if (structure == null) {
                        data.delete();
                        delete(core);
                        continue;
                    }
                    if (!structure.isStructure(core, false)) {
                        data.delete();
                        delete(core, structure.getCore().getData());
                    }
                    getDataByCore().put(core, structure.getCore().getData());
                    for (Player player : core.getNearbyPlayers(128)) {
                        player.sendBlockChange(core, structure.getCore().getData());
                    }
                    data.setCurrentTime(data.getCurrentTime() + 1);
                    if (getActivatedMachines().contains(core)) {
                        continue;
                    }
                    ((TimerMachine) machine).activate(timerManager, data, true);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public MenuItems getMenuItems() {
        return menuItems;
    }

    public HashMap<UUID, TimerData> getOutputDataByPlayer() {
        return outputDataByPlayer;
    }
}
