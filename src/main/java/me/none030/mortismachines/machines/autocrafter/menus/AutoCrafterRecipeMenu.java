package me.none030.mortismachines.machines.autocrafter.menus;

import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.machines.autocrafter.AutoCrafterData;
import me.none030.mortismachines.machines.autocrafter.AutoCrafterMachine;
import me.none030.mortismachines.machines.autocrafter.AutoCrafterManager;
import me.none030.mortismachines.machines.autocrafter.recipes.AutoCrafterDefaultRecipe;
import me.none030.mortismachines.machines.autocrafter.recipes.AutoCrafterRecipe;
import me.none030.mortismachines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class AutoCrafterRecipeMenu implements InventoryHolder {

    private final AutoCrafterManager autoCrafterManager;
    private AutoCrafterData data;
    private Inventory menu;
    private final int cancelSlot = 46;
    private final int acceptSlot = 52;
    private final int resultSlot = 49;
    private final int defaultSlot = 2;
    private final int maxSlot = 6;
    private final List<Integer> defaultGridSlots = List.of(12,13,14,21,22,23,30,31,32);
    private final List<Integer> maxGridSlots = List.of(10,11,12,13,14,15,16,19,20,21,22,23,24,25,28,29,30,31,32,33,34,37,38,39,40,41,42,43);
    private final ItemStack[] defaultGrid = new ItemStack[9];
    private final ItemStack[] maxGrid = new ItemStack[28];
    private AutoCrafterRecipe recipe;
    private boolean max = false;

    public AutoCrafterRecipeMenu(AutoCrafterManager autoCrafterManager, AutoCrafterData data) {
        this.autoCrafterManager = autoCrafterManager;
        this.data = data;
        create();
    }

    private void create() {
        Machine machine = autoCrafterManager.getMachineById().get(data.getId());
        if (!(machine instanceof AutoCrafterMachine)) {
            return;
        }
        MessageUtils utils = new MessageUtils(autoCrafterManager.getRecipeMenuItems().getTitle());
        utils.replace("%name%", ((AutoCrafterMachine) machine).getName());
        menu = Bukkit.createInventory(this, 54, Component.text(utils.getMessage()));
        update(data);
    }

    public void update(AutoCrafterData data) {
        setData(data);
        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, autoCrafterManager.getRecipeMenuItems().getItem("FILTER"));
        }
        if (max) {
            for (int i = 0; i < maxGridSlots.size(); i++) {
                int slot = maxGridSlots.get(i);
                ItemStack item = maxGrid[i];
                if (item != null) {
                    menu.setItem(slot, item);
                }else {
                    maxGrid[i] = new ItemStack(Material.AIR, 1);
                    menu.setItem(slot, new ItemStack(Material.AIR, 1));
                }
            }
        }else {
            for (int i = 0; i < defaultGridSlots.size(); i++) {
                int slot = defaultGridSlots.get(i);
                ItemStack item = defaultGrid[i];
                if (item != null) {
                    menu.setItem(slot, item);
                }else {
                    defaultGrid[i] = new ItemStack(Material.AIR, 1);
                    menu.setItem(slot, new ItemStack(Material.AIR, 1));
                }
            }
        }
        menu.setItem(defaultSlot, autoCrafterManager.getRecipeMenuItems().getItem("DEFAULT"));
        menu.setItem(maxSlot, autoCrafterManager.getRecipeMenuItems().getItem("MAX"));
        menu.setItem(cancelSlot, autoCrafterManager.getRecipeMenuItems().getItem("CANCEL"));
        menu.setItem(acceptSlot, autoCrafterManager.getRecipeMenuItems().getItem("ACCEPT"));
        updateResult();
    }

    public void updateResult() {
        Machine machine = autoCrafterManager.getMachineById().get(data.getId());
        if (!(machine instanceof AutoCrafterMachine)) {
            return;
        }
        AutoCrafterRecipe recipe;
        if (max) {
            recipe = ((AutoCrafterMachine) machine).getMaxRecipe(maxGrid);
        }else {
            Recipe defaultRecipe = Bukkit.getCraftingRecipe(defaultGrid, data.getCore().getWorld());
            if (defaultRecipe != null) {
                recipe = ((AutoCrafterMachine) machine).getDefaultRecipe(defaultRecipe);
            }else {
                recipe = null;
            }
        }
        if (recipe == null) {
            menu.setItem(resultSlot, new ItemStack(Material.AIR, 1));
        }else {
            this.recipe = recipe;
            menu.setItem(resultSlot, recipe.getResult());
        }
    }

    public void empty(Player player) {
        if (max) {
            for (int i = 0; i < maxGridSlots.size(); i++) {
                int slot = maxGridSlots.get(i);
                ItemStack item = maxGrid[i];
                if (item != null && !item.getType().equals(Material.AIR)) {
                    give(player, item);
                    maxGrid[i] = new ItemStack(Material.AIR, 1);
                    menu.setItem(slot, new ItemStack(Material.AIR, 1));
                }
            }
        }else {
            for (int i = 0; i < defaultGridSlots.size(); i++) {
                int slot = defaultGridSlots.get(i);
                ItemStack item = defaultGrid[i];
                if (item != null && !item.getType().equals(Material.AIR)) {
                    give(player, item);
                    defaultGrid[i] = new ItemStack(Material.AIR, 1);
                    menu.setItem(slot, new ItemStack(Material.AIR, 1));
                }
            }
        }
    }

    public ItemStack click(Player player, int slot, ItemStack cursor) {
        if (slot == cancelSlot) {
            close(player);
            AutoCrafterMenu menu = new AutoCrafterMenu(autoCrafterManager, data);
            menu.open(player);
            player.sendMessage(autoCrafterManager.getMessage("CANCELLED"));
        }
        if (slot == acceptSlot) {
            if (recipe != null) {
                if (data.getRecipe() != null) {
                    data.emptyGrid(player);
                    data.emptyResult(player);
                    data.emptyFuel(player);
                    data.setProcessing(false);
                }
                data.setRecipe(recipe.getKey());
                data.setMax(max);
                data.setGrid(recipe.getEmptyGrid());
                player.sendMessage(autoCrafterManager.getMessage("ACCEPTED"));
            }else {
                player.sendMessage(autoCrafterManager.getMessage("CANCELLED"));
            }
            close(player);
            AutoCrafterMenu menu = new AutoCrafterMenu(autoCrafterManager, data);
            menu.open(player);
        }
        if (slot == defaultSlot) {
            if (max) {
                empty(player);
                setMax(false);
                update(data);
            }
            player.sendMessage(autoCrafterManager.getMessage("DEFAULT"));
        }
        if (slot == maxSlot) {
            if (!max) {
                empty(player);
                setMax(true);
                update(data);
            }
            player.sendMessage(autoCrafterManager.getMessage("MAX"));
        }
        if (max) {
            if (maxGridSlots.contains(slot)) {
                int index = maxGridSlots.indexOf(slot);
                ItemStack item = maxGrid[index];
                if (item != null) {
                    if (cursor == null || cursor.getType().equals(Material.AIR)) {
                        maxGrid[index] = null;
                    }else {
                        maxGrid[index] = cursor;
                    }
                    update(data);
                    return item;
                }else {
                    if (cursor != null && !cursor.getType().equals(Material.AIR)) {
                        maxGrid[index] = cursor;
                        update(data);
                        return null;
                    }
                }
            }
        }else {
            if (defaultGridSlots.contains(slot)) {
                int index = defaultGridSlots.indexOf(slot);
                ItemStack item = defaultGrid[index];
                if (item != null) {
                    if (cursor == null || cursor.getType().equals(Material.AIR)) {
                        defaultGrid[index] = null;
                    }else {
                        defaultGrid[index] = cursor;
                    }
                    update(data);
                    return item;
                }else {
                    if (cursor != null && !cursor.getType().equals(Material.AIR)) {
                        defaultGrid[index] = cursor;
                        update(data);
                        return null;
                    }
                }
            }
        }
        return cursor;
    }

    public void onClose(Player player) {
        empty(player);
    }

    private void give(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        }else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }

    public void open(Player player) {
        player.openInventory(menu);
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public AutoCrafterManager getAutoCrafterManager() {
        return autoCrafterManager;
    }

    public AutoCrafterData getData() {
        return data;
    }

    public void setData(AutoCrafterData data) {
        this.data = data;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return menu;
    }

    public int getCancelSlot() {
        return cancelSlot;
    }

    public int getAcceptSlot() {
        return acceptSlot;
    }

    public int getDefaultSlot() {
        return defaultSlot;
    }

    public int getMaxSlot() {
        return maxSlot;
    }

    public int getResultSlot() {
        return resultSlot;
    }

    public List<Integer> getDefaultGridSlots() {
        return defaultGridSlots;
    }

    public List<Integer> getMaxGridSlots() {
        return maxGridSlots;
    }

    public ItemStack[] getDefaultGrid() {
        return defaultGrid;
    }

    public ItemStack[] getMaxGrid() {
        return maxGrid;
    }

    public AutoCrafterRecipe getRecipe() {
        return recipe;
    }

    public void setRecipe(AutoCrafterDefaultRecipe recipe) {
        this.recipe = recipe;
    }

    public boolean isMax() {
        return max;
    }

    public void setMax(boolean max) {
        this.max = max;
    }
}
