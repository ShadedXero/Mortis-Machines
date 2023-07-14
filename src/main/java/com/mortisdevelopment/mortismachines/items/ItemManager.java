package com.mortisdevelopment.mortismachines.items;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ItemManager {

    private final List<ItemStack> items;
    private final HashMap<String, ItemStack> itemById;

    public ItemManager() {
        this.items = new ArrayList<>();
        this.itemById = new HashMap<>();
    }

    public ItemStack getItem(String id) {
        ItemStack item = itemById.get(id);
        if (item == null) {
            return null;
        }
        return item.clone();
    }

    public void addItem(String id, ItemStack item) {
        itemById.put(id, item);
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public HashMap<String, ItemStack> getItemById() {
        return itemById;
    }
}
