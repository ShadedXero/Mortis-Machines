package com.mortisdevelopment.mortismachines.machines.remotecontrol;

import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.remotes.Remote;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RemoteControlMenu implements InventoryHolder {

    private final int modeSlot = 11;
    private final int assignSlot = 13;
    private final int removeSlot = 15;
    private final RemoteControlManager remoteControlManager;
    private RemoteControlData data;
    private Inventory menu;

    public RemoteControlMenu(RemoteControlManager remoteControlManager, RemoteControlData data) {
        this.remoteControlManager = remoteControlManager;
        this.data = data;
        create();
    }

    private void create() {
        menu = Bukkit.createInventory(this, 27, Component.text(remoteControlManager.getMenuItems().getTitle()));
        update(data);
    }

    public void update(RemoteControlData data) {
        setData(data);
        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, remoteControlManager.getMenuItems().getItem("FILTER"));
        }
        if (data.isPulse()) {
            menu.setItem(modeSlot, remoteControlManager.getMenuItems().getItem("PULSE_MODE"));
        }else {
            menu.setItem(modeSlot, remoteControlManager.getMenuItems().getItem("CONSTANT_MODE"));
        }
        menu.setItem(assignSlot, remoteControlManager.getMenuItems().getItem("ASSIGNER"));
        menu.setItem(removeSlot, remoteControlManager.getMenuItems().getItem("REMOVER"));
    }

    public void click(Player player, int slot, ItemStack cursor) {
        if (slot == modeSlot) {
            if (data.isPulse()) {
                data.setPulse(false);
                player.sendMessage(remoteControlManager.getMessage("CONSTANT_MODE"));
            }else {
                data.setPulse(true);
                player.sendMessage(remoteControlManager.getMessage("PULSE_MODE"));
            }
            update(data);
            Machine machine = remoteControlManager.getMachineById().get(data.getId());
            if (!(machine instanceof RemoteControlMachine)) {
                return;
            }
            data.cancelTask();
            machine.stopRedstoneSignal(data);
        }
        if (slot == assignSlot) {
            if (cursor == null || cursor.getType().equals(Material.AIR)) {
                player.sendMessage(remoteControlManager.getMessage("NULL_ITEM"));
                return;
            }
            if (data.isDetonatorLimit()) {
                player.sendMessage(remoteControlManager.getMessage("LIMIT_REACHED"));
                return;
            }
            Machine machine = remoteControlManager.getMachineById().get(data.getId());
            if (!(machine instanceof RemoteControlMachine)) {
                return;
            }
            Remote remote = ((RemoteControlMachine) machine).getRemote(cursor);
            if (remote == null) {
                player.sendMessage(remoteControlManager.getMessage("INVALID_REMOTE"));
                return;
            }
            data.addDetonator(remote, cursor);
            player.sendMessage(remoteControlManager.getMessage("ASSIGNED"));
        }
        if (slot == removeSlot) {
            data.removeDetonators();
            player.sendMessage(remoteControlManager.getMessage("REMOVED"));
        }
    }

    public void open(Player player) {
        player.openInventory(menu);
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public int getModeSlot() {
        return modeSlot;
    }

    public int getAssignSlot() {
        return assignSlot;
    }

    public int getRemoveSlot() {
        return removeSlot;
    }

    public RemoteControlManager getRemoteControlManager() {
        return remoteControlManager;
    }

    public RemoteControlData getData() {
        return data;
    }

    public void setData(RemoteControlData data) {
        this.data = data;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return menu;
    }
}
