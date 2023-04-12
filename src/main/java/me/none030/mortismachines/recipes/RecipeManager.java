package me.none030.mortismachines.recipes;

import me.none030.mortismachines.MortisMachines;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RecipeManager {

    private final List<Recipe> recipes;
    private final List<Recipe> defaultRecipes;
    private final HashMap<NamespacedKey, Recipe> defaultRecipeByKey;
    private final List<MaxRecipe> maxRecipes;
    private final HashMap<NamespacedKey,MaxRecipe> maxRecipeByKey;

    public RecipeManager() {
        this.recipes = new ArrayList<>();
        this.defaultRecipes = new ArrayList<>();
        this.defaultRecipeByKey = new HashMap<>();
        this.maxRecipes = new ArrayList<>();
        this.maxRecipeByKey = new HashMap<>();
        registerRecipes();
        MortisMachines plugin = MortisMachines.getInstance();
        plugin.getServer().getPluginManager().registerEvents(new RecipeListener(this), plugin);
    }

    public void registerRecipes() {
        for (Recipe recipe : recipes) {
            Bukkit.addRecipe(recipe);
        }
    }

    public List<Recipe> getRecipes() {
        return recipes;
    }

    public List<Recipe> getDefaultRecipes() {
        return defaultRecipes;
    }

    public HashMap<NamespacedKey, Recipe> getDefaultRecipeByKey() {
        return defaultRecipeByKey;
    }

    public List<MaxRecipe> getMaxRecipes() {
        return maxRecipes;
    }

    public HashMap<NamespacedKey,MaxRecipe> getMaxRecipeByKey() {
        return maxRecipeByKey;
    }
}
