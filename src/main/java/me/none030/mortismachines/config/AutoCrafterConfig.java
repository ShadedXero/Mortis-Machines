package me.none030.mortismachines.config;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.autocrafter.*;
import me.none030.mortismachines.machines.autocrafter.recipes.AutoCrafterMaxRecipe;
import me.none030.mortismachines.menu.MenuItems;
import me.none030.mortismachines.machines.autocrafter.recipes.AutoCrafterDefaultRecipe;
import me.none030.mortismachines.recipes.MaxRecipe;
import me.none030.mortismachines.recipes.RecipeCategory;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MachineType;
import me.none030.mortismachines.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AutoCrafterConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;
    private final HashMap<String,RecipeCategory> defaultCategoryById;
    private final HashMap<String,RecipeCategory> maxCategoryById;

    public AutoCrafterConfig(ConfigManager configManager) {
        this.configManager = configManager;
        this.defaultCategoryById = new HashMap<>();
        this.maxCategoryById = new HashMap<>();
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        ConfigurationSection section = config.getConfigurationSection("auto-crafter-machine");
        if (section == null) {
            return;
        }
        boolean enabled = section.getBoolean("enabled");
        if (!enabled) {
            return;
        }
        MenuItems menuItems = loadMenu(section.getConfigurationSection("menu"));
        if (menuItems == null) {
            plugin.getLogger().info("CONFIG WORKED");
            return;
        }
        MenuItems progressMenuItems = loadProgressMenu(section.getConfigurationSection("progress-menu"));
        if (progressMenuItems == null) {
            plugin.getLogger().info("CONFIG WORKED 2");
            return;
        }
        MenuItems recipeMenuItems = loadRecipeMenu(section.getConfigurationSection("recipe-menu"));
        if (recipeMenuItems == null) {
            plugin.getLogger().info("CONFIG WORKED 3");
            return;
        }
        configManager.getManager().setAutoCrafterManager(new AutoCrafterManager(menuItems, progressMenuItems, recipeMenuItems));
        loadMessages(section.getConfigurationSection("messages"));
        loadRecipes(section.getConfigurationSection("recipes"));
        loadMachines(section.getConfigurationSection("machines"));
        DataConfig data = new DataConfig(MachineType.AUTO_CRAFTER);
        data.load(configManager.getManager().getAutoCrafterManager());
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

    private MenuItems loadProgressMenu(ConfigurationSection progressMenu) {
        if (progressMenu == null) {
            return null;
        }
        MessageUtils title = new MessageUtils(progressMenu.getString("title"));
        title.color();
        MenuItems menuItems = new MenuItems(title.getMessage());
        for (String key : progressMenu.getKeys(false)) {
            if (key.equalsIgnoreCase("title")) {
                continue;
            }
            ItemStack item = configManager.getManager().getItemManager().getItem(progressMenu.getString(key));
            menuItems.addItem(key.replace("-", "_").toUpperCase(), item);
        }
        return menuItems;
    }

    private MenuItems loadRecipeMenu(ConfigurationSection recipeMenu) {
        if (recipeMenu == null) {
            return null;
        }
        MessageUtils title = new MessageUtils(recipeMenu.getString("title"));
        title.color();
        MenuItems menuItems = new MenuItems(title.getMessage());
        for (String key : recipeMenu.getKeys(false)) {
            if (key.equalsIgnoreCase("title")) {
                continue;
            }
            ItemStack item = configManager.getManager().getItemManager().getItem(recipeMenu.getString(key));
            menuItems.addItem(key.replace("-", "_").toUpperCase(), item);
        }
        return menuItems;
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
            configManager.getManager().getAutoCrafterManager().addMessage(id, editor.getMessage());
        }
    }

    private void loadRecipes(ConfigurationSection recipes) {
        if (recipes == null) {
            return;
        }
        ConfigurationSection defaultSection = recipes.getConfigurationSection("default");
        if (defaultSection == null) {
            return;
        }
        for (String id : defaultSection.getKeys(false)) {
            ConfigurationSection section = defaultSection.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            long time = section.getLong("time");
            long power = section.getLong("power");
            List<NamespacedKey> keys = new ArrayList<>();
            for (String recipeKey : section.getStringList("recipes")) {
                NamespacedKey key = NamespacedKey.fromString(recipeKey);
                if (key == null) {
                    continue;
                }
                keys.add(key);
            }
            RecipeCategory category = new RecipeCategory(id, time, power, keys);
            defaultCategoryById.put(id, category);
        }
        ConfigurationSection maxSection = recipes.getConfigurationSection("max");
        if (maxSection == null) {
            return;
        }
        for (String id : maxSection.getKeys(false)) {
            ConfigurationSection section = maxSection.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            long time = section.getLong("time");
            long power = section.getLong("power");
            List<NamespacedKey> keys = new ArrayList<>();
            for (String recipeKey : section.getStringList("recipes")) {
                NamespacedKey key = NamespacedKey.fromString(recipeKey);
                if (key == null) {
                    continue;
                }
                keys.add(key);
            }
            RecipeCategory category = new RecipeCategory(id, time, power, keys);
            maxCategoryById.put(id, category);
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
            MessageUtils utils = new MessageUtils(section.getString("name"));
            utils.color();
            String name = utils.getMessage();
            AutoCrafterProgressType progressType;
            try {
                progressType = AutoCrafterProgressType.valueOf(section.getString("progress-type"));
            }catch (IllegalArgumentException exp) {
                continue;
            }
            AutoCrafterRestorationType restorationType;
            try {
                restorationType = AutoCrafterRestorationType.valueOf(section.getString("restoration-type"));
            }catch (IllegalArgumentException exp) {
                continue;
            }
            boolean requireFuel = section.getBoolean("require-fuel");
            List<AutoCrafterFuel> fuels = new ArrayList<>();
            for (String rawFuel : new ArrayList<>(section.getStringList("fuels"))) {
                String[] raw = rawFuel.split(":");
                ItemStack item = configManager.getManager().getItemManager().getItem(raw[0]);
                if (item == null) {
                    continue;
                }
                long power;
                try {
                    power = Long.parseLong(raw[1]);
                }catch (NumberFormatException exp) {
                    continue;
                }
                AutoCrafterFuel fuel = new AutoCrafterFuel(item, power);
                fuels.add(fuel);
            }
            long timeMultiplier = section.getLong("time-multiplier");
            long powerMultiplier = section.getLong("power-multiplier");
            List<AutoCrafterDefaultRecipe> defaultRecipes = new ArrayList<>();
            List<AutoCrafterMaxRecipe> maxRecipes = new ArrayList<>();
            ConfigurationSection recipes = section.getConfigurationSection("recipes");
            if (recipes == null) {
                continue;
            }
            for (String id : recipes.getStringList("default")) {
                RecipeCategory category = defaultCategoryById.get(id);
                for (NamespacedKey namespacedKey : category.getKeys()) {
                    Recipe recipe = Bukkit.getRecipe(namespacedKey);
                    if (recipe == null) {
                        continue;
                    }
                    if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) {
                        continue;
                    }
                    AutoCrafterDefaultRecipe defaultRecipe = new AutoCrafterDefaultRecipe(recipe, category.multiplyTime(timeMultiplier), category.multiplyPower(powerMultiplier));
                    defaultRecipes.add(defaultRecipe);
                }
            }
            for (String id : recipes.getStringList("max")) {
                RecipeCategory category = maxCategoryById.get(id);
                for (NamespacedKey namespacedKey : category.getKeys()) {
                    MaxRecipe recipe = configManager.getManager().getRecipeManager().getMaxRecipeByKey().get(namespacedKey);
                    if (recipe == null) {
                        continue;
                    }
                    AutoCrafterMaxRecipe maxRecipe = new AutoCrafterMaxRecipe(recipe, category.multiplyTime(timeMultiplier), category.multiplyPower(powerMultiplier));
                    maxRecipes.add(maxRecipe);
                }
            }
            AutoCrafterMachine machine = new AutoCrafterMachine(key, structures, name, progressType, restorationType, requireFuel, fuels, defaultRecipes, maxRecipes);
            configManager.getManager().getAutoCrafterManager().getMachines().add(machine);
            configManager.getManager().getAutoCrafterManager().getMachineById().put(key, machine);
        }
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "autocrafter.yml");
        if (!file.exists()) {
            plugin.saveResource("autocrafter.yml", false);
        }
        return file;
    }
}
