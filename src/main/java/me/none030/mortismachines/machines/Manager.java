package me.none030.mortismachines.machines;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.config.DataConfig;
import me.none030.mortismachines.data.MachineData;
import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;

import java.util.*;

public abstract class Manager {

    private final DataConfig config;
    private final MachinePacketListener packetListener;
    private final MachineType type;
    private final List<Location> cores;
    private final List<Machine> machines;
    private final Set<Location> activatedMachines;
    private final HashMap<String, String> messageById;
    private final HashMap<String, Machine> machineById;
    private final HashMap<Location, BlockData> dataByCore;
    private final HashMap<UUID, Integer> playersInMenuCoolDown;
    private final HashMap<UUID, MachineData> dataByPlayer;

    public Manager(MachineType type, boolean packetListener, boolean listener) {
        this.type = type;
        this.config = new DataConfig(type);
        this.cores = new ArrayList<>();
        this.machines = new ArrayList<>();
        this.activatedMachines = new HashSet<>();
        this.messageById = new HashMap<>();
        this.machineById = new HashMap<>();
        this.dataByCore = new HashMap<>();
        this.playersInMenuCoolDown = new HashMap<>();
        this.dataByPlayer = new HashMap<>();
        if (listener) {
            MortisMachines plugin = MortisMachines.getInstance();
            plugin.getServer().getPluginManager().registerEvents(new MachineListener(this), plugin);
        }
        if (packetListener) {
            this.packetListener = new MachinePacketListener(this);
        }else {
            this.packetListener = null;
        }
    }

    public void delete(Location location) {
        getCores().remove(location);
        getActivatedMachines().remove(location);
        config.remove(location);
    }

    public void delete(Location location, BlockData data) {
        getCores().remove(location);
        getActivatedMachines().remove(location);
        location.getBlock().setBlockData(data);
        config.remove(location);
    }

    public void add(Location location) {
        getCores().add(location);
        config.add(location);
    }

    public void addMessage(String id, String message) {
        messageById.put(id, message);
    }

    public String getMessage(String id) {
        return messageById.get(id);
    }

    public MachineType getType() {
        return type;
    }

    public DataConfig getConfig() {
        return config;
    }

    public MachinePacketListener getPacketListener() {
        return packetListener;
    }

    public List<Location> getCores() {
        return cores;
    }

    public List<Machine> getMachines() {
        return machines;
    }

    public Set<Location> getActivatedMachines() {
        return activatedMachines;
    }

    public HashMap<String, String> getMessageById() {
        return messageById;
    }

    public HashMap<String, Machine> getMachineById() {
        return machineById;
    }

    public HashMap<Location, BlockData> getDataByCore() {
        return dataByCore;
    }

    public HashMap<UUID, Integer> getPlayersInMenuCoolDown() {
        return playersInMenuCoolDown;
    }

    public HashMap<UUID, MachineData> getDataByPlayer() {
        return dataByPlayer;
    }
}
