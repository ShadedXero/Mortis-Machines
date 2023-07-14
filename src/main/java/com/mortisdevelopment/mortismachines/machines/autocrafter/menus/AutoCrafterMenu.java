package com.mortisdevelopment.mortismachines.machines.autocrafter.menus;

import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.machines.autocrafter.AutoCrafterData;
import com.mortisdevelopment.mortismachines.machines.autocrafter.AutoCrafterMachine;
import com.mortisdevelopment.mortismachines.machines.autocrafter.AutoCrafterManager;
import com.mortisdevelopment.mortismachines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class AutoCrafterMenu implements InventoryHolder {

    private final AutoCrafterManager autoCrafterManager;
    private AutoCrafterData data;
    private Inventory menu;
    private final int progressSlot = 4;
    private final int modeSlot = 11;
    private final int recipeSlot = 13;
    private final int fuelSlot = 15;
    private final int onlineSlot = 22;

    public AutoCrafterMenu(AutoCrafterManager autoCrafterManager, AutoCrafterData data) {
        this.autoCrafterManager = autoCrafterManager;
        this.data = data;
        create();
    }

    private void create() {
        Machine machine = autoCrafterManager.getMachineById().get(data.getId());
        if (!(machine instanceof AutoCrafterMachine)) {
            return;
        }
        MessageUtils utils = new MessageUtils(autoCrafterManager.getMenuItems().getTitle());
        utils.replace("%name%", ((AutoCrafterMachine) machine).getName());
        menu = Bukkit.createInventory(this, 27, Component.text(utils.getMessage()));
        update(data);
    }

    public void update(AutoCrafterData data) {
        setData(data);
        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, autoCrafterManager.getMenuItems().getItem("FILTER"));
        }
        if (data.isManualMode()) {
            menu.setItem(modeSlot, autoCrafterManager.getMenuItems().getItem("MANUAL_MODE"));
        }else {
            menu.setItem(modeSlot, autoCrafterManager.getMenuItems().getItem("REDSTONE_MODE"));
        }
        if (data.isOnline()) {
            menu.setItem(onlineSlot, autoCrafterManager.getMenuItems().getItem("ONLINE"));
        }else {
            menu.setItem(onlineSlot, autoCrafterManager.getMenuItems().getItem("OFFLINE"));
        }
        menu.setItem(progressSlot, autoCrafterManager.getMenuItems().getItem("PROGRESS"));
        menu.setItem(recipeSlot, autoCrafterManager.getMenuItems().getItem("RECIPE"));
        Machine machine = autoCrafterManager.getMachineById().get(data.getId());
        if (!(machine instanceof AutoCrafterMachine)) {
            return;
        }
        if (((AutoCrafterMachine) machine).isRequireFuel()) {
            if (data.getFuel() != null) {
                menu.setItem(fuelSlot, data.getFuel());
            } else {
                menu.setItem(fuelSlot, autoCrafterManager.getMenuItems().getItem("NO_FUEL"));
            }
        }else {
            menu.setItem(fuelSlot, autoCrafterManager.getMenuItems().getItem("FUEL_NOT_REQUIRED"));
        }
    }

    public ItemStack click(Player player, int slot, ItemStack cursor) {
        if (slot == progressSlot) {
            AutoCrafterProgressMenu menu = new AutoCrafterProgressMenu(autoCrafterManager, data);
            menu.open(player);
            player.sendMessage(autoCrafterManager.getMessage("OPEN_PROGRESS_MENU"));
        }
        if (slot == recipeSlot) {
            AutoCrafterRecipeMenu menu = new AutoCrafterRecipeMenu(autoCrafterManager, data);
            menu.open(player);
            player.sendMessage(autoCrafterManager.getMessage("OPEN_RECIPE_MENU"));
        }
        if (slot == modeSlot) {
            if (data.isManualMode()) {
                data.setManualMode(false);
                player.sendMessage(autoCrafterManager.getMessage("REDSTONE_MODE"));
            }else {
                data.setManualMode(true);
                player.sendMessage(autoCrafterManager.getMessage("MANUAL_MODE"));
            }
            update(data);
        }
        if (slot == onlineSlot) {
            if (data.isOnline()) {
                data.setOnline(false);
                data.setOffline(true);
                player.sendMessage(autoCrafterManager.getMessage("OFFLINE"));
            }else {
                if (!data.isSetOffline()) {
                    return cursor;
                }
                data.setOnline(true);
                data.setOffline(false);
                player.sendMessage(autoCrafterManager.getMessage("ONLINE"));
            }
            update(data);
        }
        if (slot == fuelSlot) {
            Machine machine = autoCrafterManager.getMachineById().get(data.getId());
            if (!(machine instanceof AutoCrafterMachine)) {
                return cursor;
            }
            if (!((AutoCrafterMachine) machine).isRequireFuel()) {
                return cursor;
            }
            if (data.getFuel() != null) {
                if (cursor == null || cursor.getType().equals(Material.AIR)) {
                    ItemStack fuel = data.getFuel();
                    data.setFuel(null);
                    update(data);
                    return fuel;
                }else {
                    ItemStack fuel = data.getFuel();
                    data.setFuel(cursor);
                    update(data);
                    return fuel;
                }
            }else {
                if (cursor != null && !cursor.getType().equals(Material.AIR)) {
                    data.setFuel(cursor);
                    update(data);
                    return null;
                }
            }
        }
        return cursor;
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

    public int getProgressSlot() {
        return progressSlot;
    }

    public int getModeSlot() {
        return modeSlot;
    }

    public int getRecipeSlot() {
        return recipeSlot;
    }

    public int getFuelSlot() {
        return fuelSlot;
    }

    public int getOnlineSlot() {
        return onlineSlot;
    }
}
