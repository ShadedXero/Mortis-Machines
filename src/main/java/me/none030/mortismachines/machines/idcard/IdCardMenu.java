package me.none030.mortismachines.machines.idcard;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.utils.ItemEditor;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class IdCardMenu implements InventoryHolder {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final int addEditorSlot = 3;
    private final int removeEditorSlot = 5;
    private final int modeSlot = 11;
    private final int insertSlot = 13;
    private final int timeSlot = 15;
    private final int assignSlot = 22;
    private final IdCardManager idCardManager;
    private IdCardData data;
    private Inventory menu;

    public IdCardMenu(IdCardManager idCardManager, IdCardData data) {
        this.idCardManager = idCardManager;
        this.data = data;
        create();
    }

    private void create() {
        menu = Bukkit.createInventory(this, 27, Component.text(idCardManager.getMenuItems().getTitle()));
        update(data);
    }

    public void update(IdCardData data) {
        setData(data);
        for (int i = 0; i < menu.getSize(); i++) {
            menu.setItem(i, idCardManager.getMenuItems().getItem("FILTER"));
        }
        if (data.isPulse()) {
            menu.setItem(modeSlot, idCardManager.getMenuItems().getItem("PULSE_MODE"));
        }else {
            menu.setItem(modeSlot, idCardManager.getMenuItems().getItem("CONSTANT_MODE"));
        }
        if (data.getInsertKeyCard() == null) {
            menu.setItem(insertSlot, idCardManager.getMenuItems().getItem("INSERTER"));
        }else {
            menu.setItem(insertSlot, data.getInsertKeyCard());
        }
        ItemEditor editor = new ItemEditor(idCardManager.getMenuItems().getItem("TIMER"));
        editor.setPlaceholder("%time%", Long.toString(data.getTime()));
        menu.setItem(timeSlot, editor.getItem());
        menu.setItem(addEditorSlot, idCardManager.getMenuItems().getItem("EDITOR_ADDER"));
        menu.setItem(removeEditorSlot, idCardManager.getMenuItems().getItem("EDITOR_REMOVER"));
        menu.setItem(assignSlot, idCardManager.getMenuItems().getItem("ASSIGNER"));
    }

    public ItemStack click(Player player, int slot, ItemStack cursor) {
        if (slot == addEditorSlot) {
            if (data.getInsertKeyCard() == null) {
                player.sendMessage(idCardManager.getMessage("NO_KEY_CARD"));
                return cursor;
            }
            if (data.isEditorLimit()) {
                player.sendMessage(idCardManager.getMessage("EDITOR_LIMIT_REACHED"));
                return cursor;
            }
            close(player);
            player.sendMessage(idCardManager.getMessage("ADD_EDITOR"));
            idCardManager.getAddEditorByPlayer().put(player.getUniqueId(), data);
            new BukkitRunnable() {
                long count = 0;
                @Override
                public void run() {
                    count++;
                    if (idCardManager.getAddEditorByPlayer().get(player.getUniqueId()) == null) {
                        cancel();
                    }
                    if (count >= 10) {
                        player.sendMessage(idCardManager.getMessage("ADD_EDITOR_EXPIRED"));
                        idCardManager.getAddEditorByPlayer().remove(player.getUniqueId());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
        if (slot == removeEditorSlot) {
            if (data.getInsertKeyCard() == null) {
                player.sendMessage(idCardManager.getMessage("NO_KEY_CARD"));
                return cursor;
            }
            if (data.getEditors() == null) {
                player.sendMessage(idCardManager.getMessage("NO_EDITORS"));
                return cursor;
            }
            close(player);
            player.sendMessage(idCardManager.getMessage("REMOVE_EDITOR"));
            idCardManager.getRemoveEditorByPlayer().put(player.getUniqueId(), data);
            new BukkitRunnable() {
                long count = 0;
                @Override
                public void run() {
                    count++;
                    if (idCardManager.getRemoveEditorByPlayer().get(player.getUniqueId()) == null) {
                        cancel();
                    }
                    if (count >= 10) {
                        player.sendMessage(idCardManager.getMessage("REMOVE_EDITOR_EXPIRED"));
                        idCardManager.getRemoveEditorByPlayer().remove(player.getUniqueId());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
        if (slot == modeSlot) {
            if (data.getInsertKeyCard() == null) {
                player.sendMessage(idCardManager.getMessage("NO_KEY_CARD"));
                return null;
            }
            if (data.isPulse()) {
                data.setPulse(false);
                player.sendMessage(idCardManager.getMessage("CONSTANT_MODE"));
            }else {
                data.setPulse(true);
                player.sendMessage(idCardManager.getMessage("PULSE_MODE"));
            }
            update(data);
            Machine machine = idCardManager.getMachineById().get(data.getId());
            if (!(machine instanceof IdCardMachine)) {
                return cursor;
            }
            ((IdCardMachine) machine).activate(idCardManager, data);
        }
        if (slot == insertSlot) {
            if (cursor == null || cursor.getType().equals(Material.AIR)) {
                if (data.getInsertKeyCard() != null) {
                    ItemStack keyCard = data.getInsertKeyCard();
                    data.removeInsertKeyCard();
                    player.sendMessage(idCardManager.getMessage("KEY_CARD_INSERTED"));
                    update(data);
                    Machine machine = idCardManager.getMachineById().get(data.getId());
                    if (!(machine instanceof IdCardMachine)) {
                        return keyCard;
                    }
                    ((IdCardMachine) machine).deactivate(idCardManager, data);
                    return keyCard;
                }
            }else {
                if (data.getKeyCard() != null && data.getInsertKeyCard() == null) {
                    if (data.isKeyCard(cursor)) {
                        data.setInsertKeyCard();
                        cursor.setAmount(cursor.getAmount() - 1);
                        update(data);
                        Machine machine = idCardManager.getMachineById().get(data.getId());
                        if (!(machine instanceof IdCardMachine)) {
                            return cursor;
                        }
                        ((IdCardMachine) machine).activate(idCardManager, data);
                    }else {
                        player.sendMessage(idCardManager.getMessage("WRONG_KEY_CARD"));
                    }
                }
            }
        }
        if (slot == timeSlot) {
            if (data.getInsertKeyCard() == null) {
                player.sendMessage(idCardManager.getMessage("NO_KEY_CARD"));
                return cursor;
            }
            close(player);
            player.sendMessage(idCardManager.getMessage("SET_TIMER"));
            idCardManager.getDataByPlayer().put(player.getUniqueId(), data);
            new BukkitRunnable() {
                long count = 0;
                @Override
                public void run() {
                    count++;
                    if (idCardManager.getDataByPlayer().get(player.getUniqueId()) == null) {
                        cancel();
                    }
                    if (count >= 10) {
                        player.sendMessage(idCardManager.getMessage("SET_TIMER_EXPIRED"));
                        idCardManager.getDataByPlayer().remove(player.getUniqueId());
                        cancel();
                    }
                }
            }.runTaskTimer(plugin, 0L, 20L);
        }
        if (slot == assignSlot) {
            if (cursor == null || cursor.getType().equals(Material.AIR)) {
                player.sendMessage(idCardManager.getMessage("NULL_ITEM"));
                return cursor;
            }
            Machine machine = idCardManager.getMachineById().get(data.getId());
            if (!(machine instanceof IdCardMachine) || ((IdCardMachine) machine).isInBlacklist(cursor)) {
                player.sendMessage(idCardManager.getMessage("ITEM_IN_BLACKLIST"));
                return cursor;
            }
            if (data.getInsertKeyCard() != null) {
                player.sendMessage(idCardManager.getMessage("KEY_CARD_ALREADY_INSERTED"));
                return cursor;
            }
            if (!data.isEditor(player)) {
                player.sendMessage(idCardManager.getMessage("EDITOR_PERMISSION"));
                return cursor;
            }
            data.setKeyCard(cursor);
            player.sendMessage(idCardManager.getMessage("KEY_CARD_ASSIGNED"));
        }
        return cursor;
    }

    public void open(Player player) {
        player.openInventory(menu);
    }

    public void close(Player player) {
        player.closeInventory();
    }

    public int getAddEditorSlot() {
        return addEditorSlot;
    }

    public int getRemoveEditorSlot() {
        return removeEditorSlot;
    }

    public int getModeSlot() {
        return modeSlot;
    }

    public int getInsertSlot() {
        return insertSlot;
    }

    public int getTimeSlot() {
        return timeSlot;
    }

    public int getAssignSlot() {
        return assignSlot;
    }

    public IdCardManager getIdCardManager() {
        return idCardManager;
    }

    public IdCardData getData() {
        return data;
    }

    public void setData(IdCardData data) {
        this.data = data;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return menu;
    }
}
