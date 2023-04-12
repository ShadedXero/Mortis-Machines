package me.none030.mortismachines.recipes;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class MaxRecipe {

    private final NamespacedKey key;
    private final ItemStack[] ingredients;
    private final ItemStack result;

    public MaxRecipe(NamespacedKey key, ItemStack[] ingredients, ItemStack result) {
        this.key = key;
        this.result = result;
        this.ingredients = ingredients;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public ItemStack[] getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return result;
    }
}
