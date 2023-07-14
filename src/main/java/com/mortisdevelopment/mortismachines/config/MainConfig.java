package com.mortisdevelopment.mortismachines.config;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.data.DataManager;
import com.mortisdevelopment.mortismachines.data.H2Database;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class MainConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;

    public MainConfig(ConfigManager configManager) {
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadDatabase(config.getConfigurationSection("database"));
    }

    private void loadDatabase(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        String fileName = section.getString("file");
        if (fileName == null) {
            return;
        }
        File file = new File(plugin.getDataFolder(), fileName);
        String username = section.getString("username");
        String password = section.getString("password");
        configManager.getManager().setDataManager(new DataManager(new H2Database(file, username, password)));
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "config.yml");
        if (!file.exists()) {
            plugin.saveResource("config.yml", false);
        }
        return file;
    }
}
