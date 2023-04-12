package me.none030.mortismachines.config;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.idcard.IdCardMachine;
import me.none030.mortismachines.machines.idcard.IdCardManager;
import me.none030.mortismachines.menu.MenuItems;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MachineType;
import me.none030.mortismachines.utils.MessageUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class IdCardConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;

    public IdCardConfig(ConfigManager configManager) {
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("id-card-machine");
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
        DataConfig data = new DataConfig(MachineType.ID_CARD);
        data.load(configManager.getManager().getIdCardManager());
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
        configManager.getManager().setIdCardManager(new IdCardManager(menuItems));
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
            configManager.getManager().getIdCardManager().addMessage(id, editor.getMessage());
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
            long defaultTime = section.getLong("default-time");
            List<Structure> structures = new ArrayList<>();
            for (String line : new ArrayList<>(section.getStringList("structures"))) {
                Structure structure = configManager.getManager().getStructureManager().getStructureById().get(line);
                if (structure == null) {
                    continue;
                }
                structures.add(structure);
            }
            List<ItemStack> blacklist = new ArrayList<>();
            for (String line : new ArrayList<>(section.getStringList("blacklist"))) {
                ItemStack item = configManager.getManager().getItemManager().getItem(line);
                if (item == null) {
                    continue;
                }
                blacklist.add(item);
            }
            IdCardMachine machine = new IdCardMachine(key, structures, blacklist, pulse, defaultTime);
            configManager.getManager().getIdCardManager().getMachines().add(machine);
            configManager.getManager().getIdCardManager().getMachineById().put(key, machine);
        }
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "idcard.yml");
        if (!file.exists()) {
            plugin.saveResource("idcard.yml", false);
        }
        return file;
    }
}
