package me.none030.mortismachines.machines.autocrafter;

import me.none030.mortismachines.data.MachineData;
import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AutoCrafterData extends MachineData {

    private final String modeKey = "MortisMachinesManualMode";
    private final String onlineKey = "MortisMachinesOnline";
    private final String fuelKey = "MortisMachinesFuel";
    private final String resultKey = "MortisMachinesResult";
    private final String gridKey = "MortisMachinesGrid";
    private final String maxKey = "MortisMachinesMax";
    private final String recipeKey = "MortisMachinesRecipe";
    private final String timeKey = "MortisMachinesTime";
    private final String setOfflineKey = "MortisMachinesOffline";
    private final String processingKey = "MortisMachinesProcessing";
    private final String hopperTimeKey = "MortisMachinesHopperTime";
    private final String requireFuelKey = "MortisMachinesRequireFuel";

    public AutoCrafterData(Location core) {
        super(core, MachineType.AUTO_CRAFTER);
    }

    public void create(String id, String structureId, boolean manualMode, boolean online, long time, boolean requireFuel) {
        create(id, structureId);
        setManualMode(manualMode);
        setOnline(online);
        setTime(time);
        setRequireFuel(requireFuel);
    }

    public boolean isRequireFuel() {
        String value = get(requireFuelKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setRequireFuel(boolean requireFuel) {
        set(requireFuelKey, Boolean.toString(requireFuel));
    }

    public long getHopperTime() {
        String value = get(hopperTimeKey);
        if (value == null) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public void setHopperTime(long hopperTime) {
        set(hopperTimeKey, Long.toString(hopperTime));
    }

    public boolean isProcessing() {
        String value = get(processingKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setProcessing(boolean processing) {
        set(processingKey, Boolean.toString(processing));
    }

    public boolean isSetOffline() {
        String value = get(setOfflineKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setOffline(boolean setOffline) {
        set(setOfflineKey, Boolean.toString(setOffline));
    }

    public boolean isManualMode() {
        String value = get(modeKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setManualMode(boolean manualMode) {
        set(modeKey, Boolean.toString(manualMode));
    }

    public long getTime() {
        String value = get(timeKey);
        if (value == null) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public void setTime(long time) {
        set(timeKey, Long.toString(time));
    }

    public boolean isTime(long time) {
        long value = getTime();
        return value > time;
    }

    public boolean isOnline() {
        String value = get(onlineKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setOnline(boolean online) {
        set(onlineKey, Boolean.toString(online));
    }

    public boolean isMax() {
        String value = get(maxKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setMax(boolean max) {
        set(maxKey, Boolean.toString(max));
    }

    public NamespacedKey getRecipe() {
        String value = get(recipeKey);
        if (value == null) {
            return null;
        }
        return NamespacedKey.fromString(value);
    }

    public void setRecipe(NamespacedKey key) {
        if (key != null) {
            set(recipeKey, key.toString());
        }else {
            set(recipeKey, null);
        }
    }

    public ItemStack getFuel() {
        String value = get(fuelKey);
        if (value == null) {
            return null;
        }
        return deserialize(value);
    }

    public void setFuel(ItemStack item) {
        if (item == null) {
            set(fuelKey, null);
        }else {
            set(fuelKey, serialize(item));
        }
    }

    public ItemStack getResult() {
        String value = get(resultKey);
        if (value == null) {
            return null;
        }
        return deserialize(value);
    }

    public void setResult(ItemStack item) {
        if (item == null) {
            set(resultKey, null);
        }else {
            set(resultKey, serialize(item));
        }
    }

    public void addResult(ItemStack result, int amount) {
        ItemStack item = getResult();
        if (item == null) {
            item = result;
            item.setAmount(amount);
        }else {
            item.setAmount(item.getAmount() + amount);
        }
        setResult(item);
    }

    public ItemStack[] getGrid() {
        String value = get(gridKey);
        if (value == null) {
            return null;
        }
        ItemStack[] grid;
        if (isMax()) {
            grid = new ItemStack[28];
        } else {
            grid = new ItemStack[9];
        }
        String[] rawItems = value.split(",");
        for (int i = 0; i < rawItems.length; i++) {
            String rawItem = rawItems[i];
            grid[i] = deserialize(rawItem);
        }
        return grid;
    }

    public void setGrid(ItemStack[] items) {
        if (items == null || items.length == 0) {
            set(gridKey, null);
        }else {
            StringBuilder value = new StringBuilder();
            for (int i = 0; i < items.length; i++) {
                ItemStack item = items[i];
                if (items.length == (i + 1)) {
                    value.append(serialize(item));
                }else {
                    value.append(serialize(item)).append(",");
                }
            }
            set(gridKey, value.toString());
        }
    }

    public void emptyGrid(Player player) {
        ItemStack[] grid = getGrid();
        if (grid == null) {
            return;
        }
        for (ItemStack item : grid) {
            if (item == null || item.getType().equals(Material.AIR)) {
                continue;
            }
            give(player, item);
        }
        setGrid(null);
    }

    public void emptyGrid(Location location) {
        ItemStack[] grid = getGrid();
        if (grid == null) {
            return;
        }
        for (ItemStack item : grid) {
            if (item == null || item.getType().equals(Material.AIR)) {
                continue;
            }
            drop(location, item);
        }
        setGrid(null);
    }

    public void emptyResult(Player player) {
        ItemStack result = getResult();
        if (result == null) {
            return;
        }
        give(player, result);
        setResult(null);
    }

    public void emptyResult(Location location) {
        ItemStack result = getResult();
        if (result == null) {
            return;
        }
        drop(location, result);
        setResult(null);
    }

    public void emptyFuel(Player player) {
        ItemStack fuel = getFuel();
        if (fuel == null) {
            return;
        }
        give(player, fuel);
        setFuel(null);
    }

    public void emptyFuel(Location location) {
        ItemStack fuel = getFuel();
        if (fuel == null) {
            return;
        }
        drop(location, fuel);
        setFuel(null);
    }

    private void drop(Location location, ItemStack item) {
        location.getWorld().dropItemNaturally(location, item);
    }

    private void give(Player player, ItemStack item) {
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
        }else {
            player.getWorld().dropItemNaturally(player.getLocation(), item);
        }
    }
}
