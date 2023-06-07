package me.none030.mortismachines.menu;

import org.bukkit.inventory.ItemStack;

import java.util.HashMap;

public class MenuItems {

    private final String title;
    private final HashMap<String, ItemStack> itemById;

    public MenuItems(String title) {
        this.title = title;
        this.itemById = new HashMap<>();
    }

    public String getTitle() {
        return title;
    }

    public void addItem(String id, ItemStack item) {
        itemById.put(id, item);
    }

    public ItemStack getItem(String id) {
        return itemById.get(id).clone();
    }
}
