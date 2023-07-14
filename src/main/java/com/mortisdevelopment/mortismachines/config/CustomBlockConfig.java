package com.mortisdevelopment.mortismachines.config;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.customblocks.CustomBlock;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.List;

public class CustomBlockConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;

    public CustomBlockConfig(ConfigManager configManager) {
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadCustomBlocks(config.getConfigurationSection("custom-blocks"));
    }

    private void loadCustomBlocks(ConfigurationSection customBlocks) {
        if (customBlocks == null) {
            return;
        }
        for (String id : customBlocks.getKeys(false)) {
            ConfigurationSection section = customBlocks.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            String itemId = section.getString("item");
            if (itemId == null) {
                continue;
            }
            ItemStack item = configManager.getManager().getItemManager().getItem(itemId);
            if (item == null) {
                continue;
            }
            List<String> keys = section.getStringList("keys");
            CustomBlock customBlock = new CustomBlock(id, item, keys);
            configManager.getManager().getCustomBlockManager().getCustomBlockById().put(id, customBlock);
        }
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "customblocks.yml");
        if (!file.exists()) {
            plugin.saveResource("customblocks.yml", false);
        }
        return file;
    }
}
