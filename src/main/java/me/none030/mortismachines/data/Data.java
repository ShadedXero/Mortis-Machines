package me.none030.mortismachines.data;

import com.jeff_media.customblockdata.CustomBlockData;
import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class Data {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final Block block;

    public Data(Block block) {
        this.block = block;
    }

    public Data(Location location) {
        this.block = location.getBlock();
    }

    public boolean isMachine(MachineType type) {
        String machineTypeKey = get("MortisMachines");
        if (machineTypeKey == null) {
            return false;
        }
        MachineType machineType;
        try {
            machineType = MachineType.valueOf(machineTypeKey);
        }catch (IllegalArgumentException exp) {
            return false;
        }
        return machineType.equals(type);
    }

    public String get(String key) {
        CustomBlockData data = new CustomBlockData(block, plugin);
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        return data.get(namespacedKey, PersistentDataType.STRING);
    }

    public void set(String key, String value) {
        CustomBlockData data = new CustomBlockData(block, plugin);
        NamespacedKey namespacedKey = new NamespacedKey(plugin, key);
        if (value == null) {
            data.remove(namespacedKey);
        }else {
            data.set(namespacedKey, PersistentDataType.STRING, value);
        }
    }

    public void delete() {
        CustomBlockData data = new CustomBlockData(block, plugin);
        data.clear();
    }
}
