package me.none030.mortismachines.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

public class RecipeListener implements Listener {

    private final RecipeManager recipeManager;

    public RecipeListener(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent e) {
        Recipe recipe = e.getRecipe();
        NamespacedKey key = null;
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            key = shapedRecipe.getKey();
        }
        if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            key = shapelessRecipe.getKey();
        }
        if (key == null) {
            return;
        }
        for (Recipe mortisRecipe : recipeManager.getRecipes()) {
            if (mortisRecipe instanceof ShapedRecipe) {
                ShapedRecipe shapedRecipe = (ShapedRecipe) mortisRecipe;
                if (shapedRecipe.getKey().equals(key)) {
                    e.getInventory().setResult(null);
                }
            }
            if (mortisRecipe instanceof ShapelessRecipe) {
                ShapelessRecipe shapelessRecipe = (ShapelessRecipe) mortisRecipe;
                if (shapelessRecipe.getKey().equals(key)) {
                    e.getInventory().setResult(null);
                }
            }
        }
    }
}
