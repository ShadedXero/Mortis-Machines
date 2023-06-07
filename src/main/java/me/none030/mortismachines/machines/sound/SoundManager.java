package me.none030.mortismachines.machines.sound;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.machines.Manager;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SoundManager extends Manager {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final List<Location> inCoolDown;

    public SoundManager() {
        super(MachineType.SOUND, false, false);
        this.inCoolDown = new ArrayList<>();
        plugin.getServer().getPluginManager().registerEvents(new SoundListener(this), plugin);
        check();
    }

    private void check() {
        SoundManager soundManager = this;
        new BukkitRunnable() {
            @Override
            public void run() {
                for (int i = 0; i < getCores().size(); i++) {
                    Location core = getCores().get(i);
                    if (core == null) {
                        continue;
                    }
                    SoundData data = new SoundData(core);
                    if (!data.isMachine()) {
                        delete(core);
                        continue;
                    }
                    Machine machine = getMachineById().get(data.getId());
                    if (!(machine instanceof SoundMachine)) {
                        data.delete();
                        delete(core);
                        continue;
                    }
                    Structure structure = machine.getStructure(data.getStructureId());
                    if (structure == null || !structure.isStructure(core, true)) {
                        data.delete();
                        delete(core);
                        continue;
                    }
                    if (!inCoolDown.contains(core)) {
                        if (structure.hasRedstoneSignal(core)) {
                            ((SoundMachine) machine).sound(soundManager, core, data.getMessage());
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public List<Location> getInCoolDown() {
        return inCoolDown;
    }

}
