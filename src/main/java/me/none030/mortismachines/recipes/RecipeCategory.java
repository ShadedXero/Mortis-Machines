package me.none030.mortismachines.recipes;

import org.bukkit.NamespacedKey;

import java.util.List;

public class RecipeCategory {

    private final String id;
    private final long time;
    private final long power;
    private final List<NamespacedKey> keys;

    public RecipeCategory(String id, long time, long power, List<NamespacedKey> keys) {
        this.id = id;
        this.time = time;
        this.power = power;
        this.keys = keys;
    }

    public long multiplyTime(double multiplier) {
        return Math.round(time * multiplier);
    }

    public long multiplyPower(double multiplier) {
        return Math.round(power * multiplier);
    }

    public String getId() {
        return id;
    }

    public long getTime() {
        return time;
    }

    public long getPower() {
        return power;
    }

    public List<NamespacedKey> getKeys() {
        return keys;
    }
}
