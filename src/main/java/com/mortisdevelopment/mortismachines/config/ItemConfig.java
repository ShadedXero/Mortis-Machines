package com.mortisdevelopment.mortismachines.config;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.utils.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public class ItemConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;

    public ItemConfig(ConfigManager configManager) {
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadItems(config.getConfigurationSection("items"));
    }

    private void loadItems(ConfigurationSection items) {
        if (items == null) {
            return;
        }
        for (String key : items.getKeys(false)) {
            ConfigurationSection section = items.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            Material material;
            try {
                material = Material.valueOf(section.getString("material"));
            }catch (IllegalArgumentException exp) {
                continue;
            }
            int amount = section.getInt("amount");
            ItemBuilder builder = new ItemBuilder(material, amount);
            if (section.contains("custom-model-data")) {
                builder.setCustomModelData(section.getInt("custom-model-data"));
            }
            if (section.contains("name")) {
                builder.setName(section.getString("name"));
            }
            if (section.contains("lore")) {
                builder.setLore(section.getStringList("lore"));
            }
            if (section.contains("enchants")) {
                builder.addEnchants(section.getStringList("enchants"));
            }
            if (section.contains("flags")) {
                builder.addFlags(section.getStringList("flags"));
            }
            configManager.getManager().getItemManager().getItems().add(builder.getItem());
            configManager.getManager().getItemManager().addItem(key, builder.getItem());
        }
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "items.yml");
        if (!file.exists()) {
            plugin.saveResource("items.yml", false);
        }
        return file;
    }
}
