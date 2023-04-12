package me.none030.mortismachines.config;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.autocrafter.recipes.DefaultRecipeType;
import me.none030.mortismachines.recipes.MaxRecipe;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import java.io.File;
import java.util.*;

public class RecipeConfig {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ConfigManager configManager;

    public RecipeConfig(ConfigManager configManager) {
        this.configManager = configManager;
        loadConfig();
    }

    private void loadConfig() {
        File file = saveConfig();
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        loadDefaultRecipes(config.getConfigurationSection("default-recipes"));
        loadMaxRecipes(config.getConfigurationSection("max-recipes"));
    }

    private void loadDefaultRecipes(ConfigurationSection recipes) {
        if (recipes == null) {
            return;
        }
        for (String id : recipes.getKeys(false)) {
            ConfigurationSection section = recipes.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            String keyId = section.getString("key");
            if (keyId == null) {
                continue;
            }
            NamespacedKey key = NamespacedKey.fromString(keyId);
            if (key == null) {
                continue;
            }
            DefaultRecipeType type;
            try {
                type = DefaultRecipeType.valueOf(section.getString("type"));
            }catch (IllegalArgumentException exp) {
                continue;
            }
            String resultId = section.getString("result");
            if (resultId == null) {
                continue;
            }
            ItemStack result = configManager.getManager().getItemManager().getItem(resultId);
            if (result == null) {
                continue;
            }
            String raw = section.getString("ingredients");
            if (raw == null) {
                continue;
            }
            String[] rawIngredients = raw.split(",");
            if (type.equals(DefaultRecipeType.SHAPED)) {
                if (rawIngredients.length != 9) {
                    continue;
                }
            }
            ItemStack[] ingredients = new ItemStack[rawIngredients.length];
            for (int i = 0; i < rawIngredients.length; i++) {
                String rawIngredient = rawIngredients[i];
                ItemStack ingredient = configManager.getManager().getItemManager().getItem(rawIngredient);
                if (ingredient == null) {
                    ingredients[i] = new ItemStack(Material.AIR);
                    continue;
                }
                ingredients[i] = ingredient;
            }
            if (type.equals(DefaultRecipeType.SHAPED)) {
                ShapedRecipe shapedRecipe = new ShapedRecipe(key, result);
                Map<ItemStack, Character> ingredientByChar = new HashMap<>();
                Map<Character, ItemStack> charByIngredient = new HashMap<>();
                List<Character> characters = new ArrayList<>(List.of('a','b','c','d','e','f','g','h','i'));
                StringBuilder shape = new StringBuilder();
                for (int i = 0; i < 9; i++) {
                    if (i == 3 || i == 6) {
                        shape.append(":");
                    }
                    ItemStack ingredient = ingredients[i];
                    if (ingredientByChar.containsKey(ingredient)) {
                        shape.append(ingredientByChar.get(ingredient));
                        continue;
                    }
                    char character = characters.get(0);
                    characters.remove(0);
                    ingredientByChar.put(ingredient, character);
                    charByIngredient.put(character, ingredient);
                    shape.append(character);
                }
                String[] shapes = shape.toString().split(":");
                shapedRecipe.shape(shapes[0], shapes[1], shapes[2]);
                for (char character : charByIngredient.keySet()) {
                    ItemStack ingredient = charByIngredient.get(character);
                    shapedRecipe.setIngredient(character, ingredient);
                }
                configManager.getManager().getRecipeManager().getRecipes().add(shapedRecipe);
            }
            if (type.equals(DefaultRecipeType.SHAPELESS)) {
                ShapelessRecipe shapelessRecipe = new ShapelessRecipe(key, result);
                for (ItemStack ingredient : ingredients) {
                    shapelessRecipe.addIngredient(ingredient);
                }
                configManager.getManager().getRecipeManager().getRecipes().add(shapelessRecipe);
            }
        }
    }

    private void loadMaxRecipes(ConfigurationSection recipes) {
        if (recipes == null) {
            return;
        }
        for (String id : recipes.getKeys(false)) {
            ConfigurationSection section = recipes.getConfigurationSection(id);
            if (section == null) {
                continue;
            }
            String keyId = section.getString("key");
            if (keyId == null) {
                continue;
            }
            NamespacedKey key = NamespacedKey.fromString(keyId);
            if (key == null) {
                continue;
            }
            String resultId = section.getString("result");
            if (resultId == null) {
                continue;
            }
            ItemStack result = configManager.getManager().getItemManager().getItem(resultId);
            if (result == null) {
                continue;
            }
            String raw = section.getString("ingredients");
            if (raw == null) {
                continue;
            }
            String[] rawIngredients = raw.split(",");
            if (rawIngredients.length != 28) {
                continue;
            }
            ItemStack[] ingredients = new ItemStack[rawIngredients.length];
            for (int i = 0; i < rawIngredients.length; i++) {
                String rawIngredient = rawIngredients[i];
                ItemStack ingredient = configManager.getManager().getItemManager().getItem(rawIngredient);
                if (ingredient == null) {
                    ingredients[i] = new ItemStack(Material.AIR);
                    continue;
                }
                ingredients[i] = ingredient;
            }
            MaxRecipe recipe = new MaxRecipe(key, ingredients, result);
            configManager.getManager().getRecipeManager().getMaxRecipes().add(recipe);
            configManager.getManager().getRecipeManager().getMaxRecipeByKey().put(key, recipe);
        }
    }

    private File saveConfig() {
        File file = new File(plugin.getDataFolder(), "recipes.yml");
        if (!file.exists()) {
            plugin.saveResource("recipes.yml", false);
        }
        return file;
    }
}
