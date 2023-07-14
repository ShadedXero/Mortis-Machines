package com.mortisdevelopment.mortismachines.config;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.RemoteControlMachine;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.RemoteControlManager;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.remotes.*;
import com.mortisdevelopment.mortismachines.menu.MenuItems;
import com.mortisdevelopment.mortismachines.utils.MachineType;
import com.mortisdevelopment.mortismachines.utils.MessageUtils;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RemoteControlConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;

    public RemoteControlConfig(ConfigManager configManager) {
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("remote-control-machine");
        if (section == null) {
            return;
        }
        boolean enabled = section.getBoolean("enabled");
        if (!enabled) {
            return;
        }
        loadMenu(section.getConfigurationSection("menu"));
        loadMessages(section.getConfigurationSection("messages"));
        loadMachines(section.getConfigurationSection("machines"));
    }

    private void loadMenu(ConfigurationSection menu) {
        if (menu == null) {
            return;
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
        configManager.getManager().setRemoteControlManager(new RemoteControlManager(configManager.getManager().getDataManager(), menuItems));
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
            configManager.getManager().getRemoteControlManager().addMessage(id, editor.getMessage());
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
            long pulse = section.getLong("pulse");
            List<Structure> structures = new ArrayList<>();
            for (String line : new ArrayList<>(section.getStringList("structures"))) {
                Structure structure = configManager.getManager().getStructureManager().getStructureById().get(line);
                if (structure == null) {
                    continue;
                }
                structures.add(structure);
            }
            List<Remote> remotes = new ArrayList<>();
            HashMap<String, Remote> remoteById = new HashMap<>();
            ConfigurationSection remoteSection = section.getConfigurationSection("remotes");
            if (remoteSection == null) {
                continue;
            }
            for (String remoteKey : remoteSection.getKeys(false)) {
                ConfigurationSection remoteKeySection = remoteSection.getConfigurationSection(remoteKey);
                if (remoteKeySection == null) {
                    continue;
                }
                RemoteType type;
                try {
                    type = RemoteType.valueOf(remoteKeySection.getString("type"));
                }catch (IllegalArgumentException exp) {
                    continue;
                }
                ItemStack item = configManager.getManager().getItemManager().getItem(remoteKeySection.getString("remote"));
                if (item == null) {
                    continue;
                }
                int range = remoteKeySection.getInt("range");
                if (type.equals(RemoteType.FLIP_FLOP)) {
                    FlipFlopRemote remote = new FlipFlopRemote(remoteKey, item, range);
                    remotes.add(remote);
                    remoteById.put(remoteKey, remote);
                }
                if (type.equals(RemoteType.CLICKER)) {
                    long time = remoteKeySection.getLong("time");
                    ClickerRemote remote = new ClickerRemote(remoteKey, item, range, time);
                    remotes.add(remote);
                    remoteById.put(remoteKey, remote);
                }
                if (type.equals(RemoteType.DEADMAN)) {
                    long time = remoteKeySection.getLong("time");
                    DeadmanRemote remote = new DeadmanRemote(remoteKey, item, range, time);
                    remotes.add(remote);
                    remoteById.put(remoteKey, remote);
                }
            }
            RemoteControlMachine machine = new RemoteControlMachine(key, structures, pulse, remotes, remoteById);
            configManager.getManager().getRemoteControlManager().getMachines().add(machine);
            configManager.getManager().getRemoteControlManager().getMachineById().put(key, machine);
        }
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "remotecontrol.yml");
        if (!file.exists()) {
            plugin.saveResource("remotecontrol.yml", false);
        }
        return file;
    }
}
