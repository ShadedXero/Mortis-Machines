package me.none030.mortismachines.data;

import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class MachineData {

    private final Location core;
    private final MachineType type;
    private final String machineKey = "MortisMachines";
    private final String idKey = "MortisMachinesId";
    private final String structureIdKey = "MortisMachinesStructureId";
    private final String activatedKey = "MortisMachinesActivated";
    private final String taskKey = "MortisMachinesTask";

    public MachineData(Location core, MachineType type) {
        this.core = core;
        this.type = type;
    }

    public Location getCore() {
        return core;
    }

    public void create(String id, String structureId) {
        set(machineKey, type.name());
        set(idKey, id);
        set(structureIdKey, structureId);
    }

    public String getId() {
        return get(idKey);
    }

    public String getStructureId() {
        return get(structureIdKey);
    }

    public boolean isActivated() {
        String value = get(activatedKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setActivated(boolean activated) {
        set(activatedKey, Boolean.toString(activated));
    }

    public Integer getTask() {
        String value = get(taskKey);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value);
    }

    public void setTask(Integer task) {
        if (task == null) {
            set(taskKey, null);
            return;
        }
        set(taskKey, Integer.toString(task));
    }

    public void cancelTask() {
        Integer taskId = getTask();
        if (taskId == null) {
            return;
        }
        setTask(null);
        Bukkit.getScheduler().cancelTask(taskId);
    }

    public boolean isMachine() {
        Data data = new Data(core);
        return data.isMachine(type);
    }

    public String get(String id) {
        Data data = new Data(core);
        return data.get(id);
    }

    public void set(String id, String value) {
        Data data = new Data(core);
        data.set(id, value);
    }

    public void delete() {
        Data data = new Data(core);
        data.delete();
    }

    public String serialize(ItemStack item) {
        if (item == null) {
            return null;
        }
        try {
            ByteArrayOutputStream io = new ByteArrayOutputStream();
            BukkitObjectOutputStream os = new BukkitObjectOutputStream(io);
            os.writeObject(item);
            os.flush();
            return new String(Base64.getEncoder().encode(io.toByteArray()));
        }catch (IOException exp) {
            return null;
        }
    }

    public ItemStack deserialize(String rawItem) {
        if (rawItem == null) {
            return null;
        }
        try {
            ByteArrayInputStream in = new ByteArrayInputStream(Base64.getDecoder().decode(rawItem));
            BukkitObjectInputStream is = new BukkitObjectInputStream(in);
            return (ItemStack) is.readObject();
        }catch (IOException | ClassNotFoundException exp) {
            return null;
        }
    }
}
