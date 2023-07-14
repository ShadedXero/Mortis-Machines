package com.mortisdevelopment.mortismachines.machines.autocrafter.recipes;

import com.mortisdevelopment.mortismachines.recipes.MaxRecipe;

public class AutoCrafterMaxRecipe extends AutoCrafterRecipe {

    public AutoCrafterMaxRecipe(MaxRecipe recipe, long time, long power) {
        super(recipe.getKey(), recipe.getIngredients(), recipe.getResult(), time, power, 28);
    }
}