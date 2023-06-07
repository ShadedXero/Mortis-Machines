package me.none030.mortismachines.machines.remotecontrol;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.machines.Manager;
import me.none030.mortismachines.menu.MenuItems;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RemoteControlManager extends Manager {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final MenuItems menuItems;

    public RemoteControlManager(MenuItems menuItems) {
        super(MachineType.REMOTE_CONTROL, true, true);
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
