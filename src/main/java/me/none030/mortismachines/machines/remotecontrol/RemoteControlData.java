package me.none030.mortismachines.machines.remotecontrol;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.data.MachineData;
import me.none030.mortismachines.machines.remotecontrol.remotes.Remote;
import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public class RemoteControlData extends MachineData {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final Location core;
    private final String pulseKey = "MortisMachinesPulse";
    private final String machineActivatedKey = "MortisMachinesMachineActivated";
    private final String detonatorsKey = "MortisMachinesDetonators";

    public RemoteControlData(Location core) {
        super(core, MachineType.REMOTE_CONTROL);
        this.core = core;
    }

    public void create(String id, String structureId, boolean pulse) {
        create(id, structureId);
        setPulse(pulse);
    }

    public void setMachineActivated(boolean machineActivated) {
        set(machineActivatedKey, Boolean.toString(machineActivated));
    }

    public boolean isMachineActivated() {
        String value = get(machineActivatedKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setPulse(boolean pulse) {
        set(pulseKey, Boolean.toString(pulse));
    }

    public boolean isPulse() {
        String value = get(pulseKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void addDetonator(Remote remote, ItemStack item) {
        String uuid = createDetonator(remote, item);
        if (uuid == null) {
            return;
        }
        String rawDetonators = get(detonatorsKey);
        if (rawDetonators == null) {
            set(detonatorsKey, uuid);
        }else {
            set(detonatorsKey, rawDetonators + "," + uuid);
        }
    }

    public boolean isDetonator(ItemStack item) {
        String uuid = getDetonatorId(item);
        if (uuid == null) {
            return false;
        }
        String raw = get(detonatorsKey);
        if (raw == null) {
            return false;
        }
        String[] rawDetonators = raw.split(",");
        for (String rawDetonator : rawDetonators) {
            if (rawDetonator.equalsIgnoreCase(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDetonatorLimit() {
        String raw = get(detonatorsKey);
        if (raw == null) {
            return false;
        }
        String[] rawDetonators = raw.split(",");
        return rawDetonators.length >= 10;
    }

    public void removeDetonators() {
        set(detonatorsKey, null);
    }

    private String getDetonatorId(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "MortisMachinesDetonator"), PersistentDataType.STRING);
    }

    private String createDetonator(Remote remote, ItemStack item) {
        if (item == null) {
            return null;
        }
        String uuid = UUID.randomUUID().toString();
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "MortisMachinesId"), PersistentDataType.STRING, remote.getId());
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "MortisMachinesCore"), PersistentDataType.STRING, core.getWorld().getName() + "," + core.getX() + "," + core.getY() + "," + core.getZ());
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "MortisMachinesDetonator"), PersistentDataType.STRING, uuid);
        item.setItemMeta(meta);
        return uuid;
    }
}
