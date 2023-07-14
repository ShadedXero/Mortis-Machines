package com.mortisdevelopment.mortismachines.machines.sound;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.data.DataManager;
import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.machines.Manager;
import com.mortisdevelopment.mortismachines.utils.MachineType;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SoundManager extends Manager {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final List<Location> inCoolDown;

    public SoundManager(DataManager dataManager) {
        super(MachineType.SOUND, dataManager, false, false);
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
