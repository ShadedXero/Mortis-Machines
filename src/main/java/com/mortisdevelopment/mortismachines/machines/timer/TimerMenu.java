package com.mortisdevelopment.mortismachines.machines.timer;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.utils.ItemEditor;
import com.mortisdevelopment.mortismachines.utils.MessageUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class TimerMenu implements InventoryHolder {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final int timeSlot = 11;
    private final int pulseSlot = 13;
    private final int outputSlot = 15;
    private final TimerManager timerManager;
    private TimerData data;
    private Inventory menu;

    public TimerMenu(TimerManager timerManager, TimerData data) {
        this.timerManager = timerManager;
        this.data = data;
        create();
    }

    private void create() {
        menu = Bukkit.createInventory(this, 27, Component.text(timerManager.getMenuItems().getTitle()));
        update(data);
    }

    public void update(TimerData data) {
        setData(data);
        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, timerManager.getMenuItems().getItem("FILTER"));
        }
        if (data.isPulse()) {
            menu.setItem(pulseSlot, timerManager.getMenuItems().getItem("PULSE_MODE"));
        }else {
            menu.setItem(pulseSlot, timerManager.getMenuItems().getItem("CONSTANT_MODE"));
        }
        ItemEditor editor = new ItemEditor(timerManager.getMenuItems().getItem("TIMER"));
        if (data.getTime().isLocal()) {
            MessageUtils utils = new MessageUtils("");
            editor.setPlaceholder("%time%", utils.getString(data.getTime().getLocalTime()));
        }else {
            editor.setPlaceholder("%time%", Long.toString(data.getTime().getTime()));
        }
        menu.setItem(timeSlot, editor.getItem());
        ItemEditor outputTime = new ItemEditor(timerManager.getMenuItems().getItem("OUTPUT_TIMER"));
        if (data.getOutputTime() == -1) {
            outputTime.setPlaceholder("%time%", "Infinite");
        }else {
            outputTime.setPlaceholder("%time%", Long.toString(data.getOutputTime()));
        }
        menu.setItem(outputSlot, outputTime.getItem());
    }

    public void click(Player player, int slot) {
        if (slot == pulseSlot) {
            if (data.isPulse()) {
                data.setPulse(false);
                player.sendMessage(timerManager.getMessage("CONSTANT_MODE"));
            }else {
                data.setPulse(true);
                player.sendMessage(timerManager.getMessage("PULSE_MODE"));
            }
            update(data);
            Machine machine = timerManager.getMachineById().get(data.getId());
            if (!(machine instanceof TimerMachine)) {
                return;
            }
            ((TimerMachine) machine).activate(timerManager, data, true);
        }
        if (slot == timeSlot) {
            close(player);
            timerManager.getDataByPlayer().put(player.getUniqueId(), data);
            player.sendMessage(timerManager.getMessage("SET_TIMER"));
            new BukkitRunnable() {
                long count = 0;
                @Override
                public void run() {
                    count++;
                    if (timerManager.getDataByPlayer().get(player.getUniqueId()) == null) {
                        cancel();
                    }
                    if (count >= 10) {
                        timerManager.getDataByPlayer().remove(player.getUniqueId());
                        player.sendMessage(timerManager.getMessage("SET_TIMER_EXPIRED"));
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
        if (slot == outputSlot) {
            close(player);
            timerManager.getOutputDataByPlayer().put(player.getUniqueId(), data);
            player.sendMessage(timerManager.getMessage("SET_OUTPUT_TIMER"));
            new BukkitRunnable() {
                long count = 0;
                @Override
                public void run() {
                    count++;
                    if (timerManager.getOutputDataByPlayer().get(player.getUniqueId()) == null) {
                        cancel();
                    }
                    if (count >= 10) {
                        timerManager.getOutputDataByPlayer().remove(player.getUniqueId());
                        player.sendMessage(timerManager.getMessage("SET_OUTPUT_TIMER_EXPIRED"));
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
    }

    public void open(Player player) {
        player.openInventory(menu);
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public int getPulseSlot() {
        return pulseSlot;
    }

    public int getOutputSlot() {
        return outputSlot;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public TimerData getData() {
        return data;
    }

    public void setData(TimerData data) {
        this.data = data;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return menu;
    }
}
