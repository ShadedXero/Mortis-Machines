package com.mortisdevelopment.mortismachines.machines.autocrafter;

import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.machines.autocrafter.recipes.AutoCrafterDefaultRecipe;
import com.mortisdevelopment.mortismachines.machines.autocrafter.recipes.AutoCrafterMaxRecipe;
import com.mortisdevelopment.mortismachines.machines.autocrafter.menus.AutoCrafterProgressMenu;
import com.mortisdevelopment.mortismachines.machines.autocrafter.recipes.AutoCrafterRecipe;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Hopper;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;

import java.util.List;

public class AutoCrafterMachine extends Machine {

    private final String name;
    private final AutoCrafterProgressType progressType;
    private final AutoCrafterRestorationType restorationType;
    private final boolean requireFuel;
    private final List<AutoCrafterFuel> fuels;
    private final List<AutoCrafterDefaultRecipe> defaultRecipes;
    private final List<AutoCrafterMaxRecipe> maxRecipes;

    public AutoCrafterMachine(String id, List<Structure> structures, String name, AutoCrafterProgressType progressType, AutoCrafterRestorationType restorationType, boolean requireFuel, List<AutoCrafterFuel> fuels, List<AutoCrafterDefaultRecipe> defaultRecipes, List<AutoCrafterMaxRecipe> maxRecipes) {
        super(id, structures);
        this.name = name;
        this.progressType = progressType;
        this.restorationType = restorationType;
        this.requireFuel = requireFuel;
        this.fuels = fuels;
        this.defaultRecipes = defaultRecipes;
        this.maxRecipes = maxRecipes;
    }

    public void checkFuel(AutoCrafterData data, Structure structure) {
        List<Hopper> hoppers = structure.getInHoppers(data.getCore());
        for (Hopper hopper : hoppers) {
            if (isFull(data.getFuel())) {
                return;
            }else {
                if (requireFuel) {
                    addFuel(hopper.getInventory(), data);
                }
            }
        }
    }

    public void checkInHoppers(AutoCrafterData data, Structure structure, AutoCrafterRecipe recipe) {
        List<Hopper> hoppers = structure.getInHoppers(data.getCore());
        for (Hopper hopper : hoppers) {
            if (recipe.isFull(data.getGrid())) {
                return;
            }else {
                if (restorationType.equals(AutoCrafterRestorationType.STACK)) {
                    recipe.addItemsByStack(hopper.getInventory(), data);
                }else {
                    recipe.addItemsBySingle(hopper.getInventory(), data);
                }
                if (requireFuel) {
                    addFuel(hopper.getInventory(), data);
                }
            }
        }
    }

    public void checkOutHoppers(AutoCrafterData data, Structure structure, AutoCrafterRecipe recipe) {
        List<Hopper> hoppers = structure.getOutHoppers(data.getCore());
        for (Hopper hopper : hoppers) {
            if (data.getResult() == null) {
                return;
            }else {
                recipe.removeItems(hopper.getInventory(), data);
            }
        }
    }

    public void check(AutoCrafterData data, Structure structure, AutoCrafterRecipe recipe, AutoCrafterFuel fuel) {
        data.setHopperTime(data.getHopperTime() + 1);
        if (data.getHopperTime() > 10) {
            checkInHoppers(data, structure, recipe);
            checkOutHoppers(data, structure, recipe);
            data.setHopperTime(0);
        }
        ItemStack result = data.getResult();
        if (result != null) {
            int amount = result.getAmount() + recipe.getResult().getAmount();
            if (amount > result.getMaxStackSize()) {
                return;
            }
        }
        if (!data.isProcessing()) {
            if (recipe.isRecipe(data.getGrid())) {
                recipe.craft(data);
                if (requireFuel) {
                    fuel.consume(data, recipe.getPower());
                }
            }else {
                return;
            }
            data.setProcessing(true);
            animate(recipe.getTime());
        }
        data.setTime(data.getTime() + 1);
        if (!data.isTime(recipe.getTime())) {
            return;
        }
        data.addResult(recipe.getResult(), recipe.getResult().getAmount());
        data.setTime(0);
        data.setProcessing(false);
    }

    public void addFuel(Inventory inv, AutoCrafterData data) {
        if (inv.isEmpty()) {
            return;
        }
        ItemStack fuelItem = data.getFuel();
        if (fuelItem == null || fuelItem.getType().equals(Material.AIR)) {
            for (ItemStack item : inv.getContents()) {
                if (item == null || item.getType().equals(Material.AIR)) {
                    continue;
                }
                AutoCrafterFuel fuel = getFuel(item);
                if (fuel == null) {
                    continue;
                }
                inv.removeItem(item);
                data.setFuel(item);
                return;
            }
        }else {
            if (isFull(fuelItem)) {
                return;
            }
            AutoCrafterFuel fuel = getFuel(fuelItem);
            if (fuel == null) {
                return;
            }
            for (ItemStack item : inv.getContents()) {
                if (item == null || item.getType().equals(Material.AIR)|| !item.isSimilar(fuelItem)) {
                    continue;
                }
                int itemAmount = item.getAmount();
                int fuelItemAmount = fuelItem.getAmount();
                int maxAmount = fuelItem.getMaxStackSize();
                int space = maxAmount - fuelItemAmount;
                if (itemAmount > space) {
                    item.setAmount(item.getAmount() - space);
                    ItemStack cloned = item.clone();
                    cloned.setAmount(maxAmount);
                    data.setFuel(cloned);
                }else {
                    inv.removeItem(item);
                    item.setAmount(fuelItemAmount + itemAmount);
                    data.setFuel(item);
                }
                return;
            }
        }
    }

    public boolean isFull(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getAmount() >= item.getMaxStackSize();
    }

    public void animate(long time) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            Inventory inv = player.getOpenInventory().getTopInventory();
            if (inv.getHolder() instanceof AutoCrafterProgressMenu) {
                AutoCrafterProgressMenu menu = (AutoCrafterProgressMenu) inv.getHolder();
                menu.animate(time);
            }
        }
    }

    public AutoCrafterFuel getFuel(ItemStack item) {
        if (item == null) {
            return null;
        }
        for (AutoCrafterFuel fuel : fuels) {
            if (fuel.isFuel(item)) {
                return fuel;
            }
        }
        return null;
    }

    public AutoCrafterMaxRecipe getMaxRecipe(NamespacedKey key) {
        for (AutoCrafterMaxRecipe maxRecipe : this.maxRecipes) {
            if (maxRecipe.isKey(key)) {
                return maxRecipe;
            }
        }
        return null;
    }

    public AutoCrafterMaxRecipe getMaxRecipe(ItemStack[] ingredients) {
        for (AutoCrafterMaxRecipe maxRecipe : this.maxRecipes) {
            if (maxRecipe.isRecipe(ingredients)) {
                return maxRecipe;
            }
        }
        return null;
    }

    public AutoCrafterDefaultRecipe getDefaultRecipe(Recipe recipe) {
        for (AutoCrafterDefaultRecipe defaultRecipe : this.defaultRecipes) {
            if (defaultRecipe.isRecipe(recipe)) {
                return defaultRecipe;
            }
        }
        return null;
    }

    public AutoCrafterDefaultRecipe getDefaultRecipe(NamespacedKey key) {
        for (AutoCrafterDefaultRecipe defaultRecipe : this.defaultRecipes) {
            if (defaultRecipe.isKey(key)) {
                return defaultRecipe;
            }
        }
        return null;
    }

    public AutoCrafterRecipe getRecipe(NamespacedKey key) {
        AutoCrafterDefaultRecipe defaultRecipe = getDefaultRecipe(key);
        if (defaultRecipe == null) {
            return getMaxRecipe(key);
        }
        return defaultRecipe;
    }

    public String getName() {
        return name;
    }

    public AutoCrafterProgressType getProgressType() {
        return progressType;
    }

    public AutoCrafterRestorationType getRestorationType() {
        return restorationType;
    }

    public boolean isRequireFuel() {
        return requireFuel;
    }

    public List<AutoCrafterFuel> getFuels() {
        return fuels;
    }

    public List<AutoCrafterDefaultRecipe> getDefaultRecipes() {
        return defaultRecipes;
    }

    public List<AutoCrafterMaxRecipe> getMaxRecipes() {
        return maxRecipes;
    }
}
