package com.mortisdevelopment.mortismachines.machines.autocrafter;

import org.bukkit.inventory.ItemStack;

public class AutoCrafterFuel {

    private final ItemStack item;
    private final long power;

    public AutoCrafterFuel(ItemStack item, long power) {
        this.item = item;
        this.power = power;
    }

    public long getPower(ItemStack item) {
        if (!isFuel(item)) {
            return 0;
        }
        return power * item.getAmount();
    }

    public void consume(AutoCrafterData data, long power) {
        ItemStack fuel = data.getFuel();
        if (fuel == null) {
            return;
        }
        int amount = fuel.getAmount();
        int powerAmount = (int) Math.ceil(power / this.power);
        if (amount < powerAmount) {
            return;
        }
        if (powerAmount > 0) {
            fuel.setAmount(amount - powerAmount);
        }else {
            fuel.setAmount(amount - 1);
        }
        if (fuel.getAmount() > 0) {
            data.setFuel(fuel);
        }else {
            data.setFuel(null);
        }
    }

    public boolean isFuel(ItemStack item) {
        return item.isSimilar(this.item);
    }

    public ItemStack getItem() {
        return item;
    }

    public long getPower() {
        return power;
    }
}
