package com.mortisdevelopment.mortismachines.config;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.menu.MenuItems;
import com.mortisdevelopment.mortismachines.utils.MessageUtils;
import com.mortisdevelopment.mortismachines.machines.timer.TimerMachine;
import com.mortisdevelopment.mortismachines.machines.timer.TimerManager;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TimerConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;

    public TimerConfig(ConfigManager configManager) {
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("timer-machine");
        if (section == null) {
            return;
        }
        boolean enabled = section.getBoolean("enabled");
        if (!enabled) {
            return;
        }
        MenuItems menu = loadMenu(section.getConfigurationSection("menu"));
        if (menu == null) {
            return;
        }
        configManager.getManager().setTimerManager(new TimerManager(configManager.getManager().getDataManager(), menu));
        loadMachine(section.getConfigurationSection("machine"));
        loadMessages(section.getConfigurationSection("messages"));
    }

    private MenuItems loadMenu(ConfigurationSection menu) {
        if (menu == null) {
            return null;
        }
        MessageUtils title = new MessageUtils(menu.getString("title"));
        title.color();
        MenuItems menuItems = new MenuItems(title.getMessage());
        for (String key : menu.getKeys(false)) {
            if (key.equalsIgnoreCase("title")) {
                continue;
            }
            ItemStack item = configManager.getManager().getItemManager().getItem(menu.getString(key));
            menuItems.addItem(key.replace("-", "_").toUpperCase(), item);
        }
        return menuItems;
    }

    private void loadMachine(ConfigurationSection section) {
        if (section == null) {
            return;
        }
        long pulse = section.getLong("pulse");
        long defaultTime = section.getLong("default-time");
        long defaultOutputTime = section.getLong("default-output-time");
        List<Structure> structures = new ArrayList<>();
        for (String line : new ArrayList<>(section.getStringList("structures"))) {
            Structure structure = configManager.getManager().getStructureManager().getStructureById().get(line);
            if (structure == null) {
                continue;
            }
            structures.add(structure);
        }
        TimerMachine machine = new TimerMachine("TIMER_MACHINE", structures, pulse, defaultTime, defaultOutputTime);
        configManager.getManager().getTimerManager().getMachines().add(machine);
        configManager.getManager().getTimerManager().getMachineById().put("TIMER_MACHINE", machine);
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
            configManager.getManager().getTimerManager().addMessage(id, editor.getMessage());
        }
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "timer.yml");
        if (!file.exists()) {
            plugin.saveResource("timer.yml", false);
        }
        return file;
    }
}
