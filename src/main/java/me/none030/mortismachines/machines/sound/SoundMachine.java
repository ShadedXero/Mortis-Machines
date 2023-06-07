package me.none030.mortismachines.machines.sound;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.structures.Structure;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public class SoundMachine extends Machine {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final int range;
    private final long cooldown;
    private final List<String> blacklist;

    public SoundMachine(String id, int range, long cooldown, List<String> blacklist, List<Structure> structures) {
        super(id, structures);
        this.range = range;
        this.cooldown = cooldown;
        this.blacklist = blacklist;
    }

    public void sound(SoundManager soundManager, Location location, String message) {
        if (message == null) {
            return;
        }
        if (range == 0) {
            Bukkit.broadcast(Component.text(message));
        }else {
            for (Player player : location.getNearbyPlayers(range)) {
                player.sendMessage(message);
            }
        }
        setCooldown(soundManager, location);
    }

    public boolean isInBlacklist(String message) {
        for (String word : blacklist) {
            if (message.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private void setCooldown(SoundManager soundManager, Location location) {
        if (cooldown <= 0) {
            return;
        }
        soundManager.getInCoolDown().add(location);
        new BukkitRunnable() {
            @Override
            public void run() {
                soundManager.getInCoolDown().remove(location);
            }
        }.runTaskLater(plugin, (cooldown * 20));
    }

    public int getRange() {
        return range;
    }

    public long getCooldown() {
        return cooldown;
    }

    public List<String> getBlacklist() {
        return blacklist;
    }
}
