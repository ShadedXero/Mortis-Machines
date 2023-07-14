package com.mortisdevelopment.mortismachines.config;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.machines.sound.SoundMachine;
import com.mortisdevelopment.mortismachines.machines.sound.SoundManager;
import com.mortisdevelopment.mortismachines.utils.MessageUtils;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SoundConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;

    public SoundConfig(ConfigManager configManager) {
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("sound-machine");
        if (section == null) {
            return;
        }
        boolean enabled = section.getBoolean("enabled");
        if (!enabled) {
            return;
        }
        configManager.getManager().setSoundManager(new SoundManager(configManager.getManager().getDataManager()));
        loadMessages(section.getConfigurationSection("messages"));
        loadMachines(section.getConfigurationSection("machines"));
    }

    private void loadMessages(ConfigurationSection messages) {
        if (messages == null) {
            return;
        }
        for (String key : messages.getKeys(false)) {
            String id = key.replace("-", "_").toUpperCase();
            String message = messages.getString(key);
            MessageUtils editor = new MessageUtils(message);
            editor.color();
            configManager.getManager().getSoundManager().addMessage(id, editor.getMessage());
        }
    }

    private void loadMachines(ConfigurationSection machines) {
        if (machines == null) {
            return;
        }
        for (String key : machines.getKeys(false)) {
            ConfigurationSection section = machines.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            List<Structure> structures = new ArrayList<>();
            for (String line : new ArrayList<>(section.getStringList("structures"))) {
                Structure structure = configManager.getManager().getStructureManager().getStructureById().get(line);
                if (structure == null) {
                    continue;
                }
                structures.add(structure);
            }
            int range = section.getInt("range");
            long cooldown = section.getLong("cooldown");
            List<String> blacklist = new ArrayList<>(section.getStringList("blacklist"));
            SoundMachine machine = new SoundMachine(key, range, cooldown, blacklist, structures);
            configManager.getManager().getSoundManager().getMachines().add(machine);
            configManager.getManager().getSoundManager().getMachineById().put(key, machine);
        }
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "sound.yml");
        if (!file.exists()) {
            plugin.saveResource("sound.yml", false);
        }
        return file;
    }
}
