package com.mortisdevelopment.mortismachines.machines.autocrafter.recipes;

import org.bukkit.inventory.*;

public class AutoCrafterDefaultRecipe extends AutoCrafterRecipe{

    public AutoCrafterDefaultRecipe(Recipe recipe, long time, long power) {
        super(recipe, time, power, 9);
    }

    public boolean isRecipe(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            return shapedRecipe.getKey().equals(getKey());
        }
        if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            return shapelessRecipe.getKey().equals(getKey());
        }
        return false;
    }
}
