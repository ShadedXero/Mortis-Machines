package me.none030.mortismachines.machines.remotecontrol.remotes;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public abstract class Remote {

    private final String id;
    private final ItemStack remote;
    private final int range;

    public Remote(String id, ItemStack remote, int range) {
        this.id = id;
        this.remote = remote;
        this.range = range;
    }

    public boolean isInRange(UUID uuid, Location location) {
        if (range <= 0) {
            return true;
        }
        for (Player player : location.getNearbyPlayers(range)) {
            if (player.getUniqueId().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRemote(ItemStack item) {
        return item.isSimilar(remote);
    }

    public String getId() {
        return id;
    }

    public ItemStack getRemote() {
        return remote;
    }

    public int getRange() {
        return range;
    }
}
