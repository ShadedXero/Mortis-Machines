package com.mortisdevelopment.mortismachines.machines.remotecontrol;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.data.DataManager;
import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.machines.Manager;
import com.mortisdevelopment.mortismachines.utils.MachineType;
import com.mortisdevelopment.mortismachines.menu.MenuItems;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RemoteControlManager extends Manager {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final MenuItems menuItems;

    public RemoteControlManager(DataManager dataManager, MenuItems menuItems) {
        super(MachineType.REMOTE_CONTROL, dataManager, true, true);
        this.menuItems = menuItems;
        plugin.getServer().getPluginManager().registerEvents(new RemoteControlListener(this), plugin);
        check();
    }

    private void check() {
        RemoteControlManager remoteControlManager = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                getPlayersInMenuCoolDown().clear();
                for (int i = 0; i < getCores().size(); i++) {
                    Location core = getCores().get(i);
                    if (core == null) {
                        continue;
                    }
                    RemoteControlData data = new RemoteControlData(core);
                    if (!data.isMachine()) {
                        delete(core);
                        continue;
                    }
                    Machine machine = getMachineById().get(data.getId());
                    if (!(machine instanceof RemoteControlMachine)) {
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
                    for (Player player : core.getNearbyPlayers(128)) {
                        player.sendBlockChange(core, structure.getCore().getData());
                    }
                    getDataByCore().put(core, structure.getCore().getData());
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public MenuItems getMenuItems() {
        return menuItems;
    }
}
