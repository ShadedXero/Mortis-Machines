package me.none030.mortismachines.config;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.Manager;
import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final MachineType type;

    public DataConfig(MachineType type) {
        this.type = type;
    }

    public void load(Manager manager) {
        File file = getFile();
        if (file == null) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = getSection();
        if (key == null) {
            return;
        }
        List<String> locations = new ArrayList<>(config.getStringList(key));
        for (String line : locations) {
            String[] raw = line.split(",");
            World world = Bukkit.getWorld(raw[0]);
            if (world == null) {
                remove(line);
                continue;
            }
            Location loc = new Location(world, Double.parseDouble(raw[1]), Double.parseDouble(raw[2]), Double.parseDouble(raw[3]));
            manager.getCores().add(loc);
        }
    }

    public void add(Location location) {
        File file = getFile();
        if (file == null) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = getSection();
        if (key == null) {
            return;
        }
        List<String> locations = new ArrayList<>(config.getStringList(key));
        String loc = location.getWorld().getName() + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ();
        locations.add(loc);
        config.set(key, locations);
        try {
            config.save(file);
        }catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public void add(String loc) {
        File file = getFile();
        if (file == null) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = getSection();
        if (key == null) {
            return;
        }
        List<String> locations = new ArrayList<>(config.getStringList(key));
        locations.add(loc);
        config.set(key, locations);
        try {
            config.save(file);
        }catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public void remove(Location location) {
        File file = getFile();
        if (file == null) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = getSection();
        if (key == null) {
            return;
        }
        List<String> locations = new ArrayList<>(config.getStringList(key));
        String loc = location.getWorld().getName() + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ();
        locations.remove(loc);
        config.set(key, locations);
        try {
            config.save(file);
        }catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    public void remove(String loc) {
        File file = getFile();
        if (file == null) {
            return;
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        String key = getSection();
        if (key == null) {
            return;
        }
        List<String> locations = new ArrayList<>(config.getStringList(key));
        locations.remove(loc);
        config.set(key, locations);
        try {
            config.save(file);
        }catch (IOException exp) {
            exp.printStackTrace();
        }
    }

    private String getSection() {
        if (type.equals(MachineType.SOUND)) return "sounds";
        if (type.equals(MachineType.TIMER)) return "timers";
        if (type.equals(MachineType.ID_CARD)) return "idcards";
        if (type.equals(MachineType.AUTO_CRAFTER)) return "autocrafters";
        if (type.equals(MachineType.REMOTE_CONTROL)) return "remotecontrols";
        return null;
    }

    private File getFile() {
        if (type.equals(MachineType.SOUND)) return saveSoundFile();
        if (type.equals(MachineType.TIMER)) return saveTimerFile();
        if (type.equals(MachineType.ID_CARD)) return saveIdCardFile();
        if (type.equals(MachineType.AUTO_CRAFTER)) return saveAutoCrafterFile();
        if (type.equals(MachineType.REMOTE_CONTROL)) return saveRemoteControlFile();
        return null;
    }

    private File saveSoundFile() {
        File file = new File(plugin.getDataFolder() + "/data/", "sound.yml");
        if (!file.exists()) {
            plugin.saveResource("data/sound.yml", false);
        }
        return file;
    }

    private File saveIdCardFile() {
        File file = new File(plugin.getDataFolder() + "/data/", "idcard.yml");
        if (!file.exists()) {
            plugin.saveResource("data/idcard.yml", false);
        }
        return file;
    }

    private File saveTimerFile() {
        File file = new File(plugin.getDataFolder() + "/data/", "timer.yml");
        if (!file.exists()) {
            plugin.saveResource("data/timer.yml", false);
        }
        return file;
    }

    private File saveAutoCrafterFile() {
        File file = new File(plugin.getDataFolder() + "/data/", "autocrafter.yml");
        if (!file.exists()) {
            plugin.saveResource("data/autocrafter.yml", false);
        }
        return file;
    }

    private File saveRemoteControlFile() {
        File file = new File(plugin.getDataFolder() + "/data/", "remotecontrol.yml");
        if (!file.exists()) {
            plugin.saveResource("data/remotecontrol.yml", false);
        }
        return file;
    }
}
