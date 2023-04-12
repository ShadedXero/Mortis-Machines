package me.none030.mortismachines.machines.autocrafter.recipes;

import me.none030.mortismachines.recipes.MaxRecipe;

public class AutoCrafterMaxRecipe extends AutoCrafterRecipe {

    public AutoCrafterMaxRecipe(MaxRecipe recipe, long time, long power) {
        super(recipe.getKey(), recipe.getIngredients(), recipe.getResult(), time, power, 28);
    }
}