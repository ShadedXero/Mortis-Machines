package me.none030.mortismachines.machines.autocrafter;

import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.machines.autocrafter.menus.AutoCrafterMenu;
import me.none030.mortismachines.machines.autocrafter.menus.AutoCrafterProgressMenu;
import me.none030.mortismachines.machines.autocrafter.menus.AutoCrafterRecipeMenu;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class AutoCrafterListener implements Listener {

    private final AutoCrafterManager autoCrafterManager;

    public AutoCrafterListener(AutoCrafterManager autoCrafterManager) {
        this.autoCrafterManager = autoCrafterManager;
    }

    @EventHandler
    public void onMachineBuild(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player player = e.getPlayer();
        Location location = e.getBlockPlaced().getLocation();
        for (Machine machine : autoCrafterManager.getMachines()) {
            if (!(machine instanceof AutoCrafterMachine)) {
                continue;
            }
            Structure structure = machine.getStructure(location, true);
            if (structure != null) {
                AutoCrafterData data = new AutoCrafterData(location);
                data.create(machine.getId(), structure.getId(), true, false, 0, ((AutoCrafterMachine) machine).isRequireFuel());
                autoCrafterManager.add(location);
                player.sendMessage(autoCrafterManager.getMessage("MACHINE_BUILD"));
                return;
            }
        }
    }

    @EventHandler
    public void onMachineInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        if (e.useInteractedBlock().equals(Event.Result.DENY) || !e.getAction().isRightClick() || player.isSneaking()) {
            return;
        }
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!autoCrafterManager.getCores().contains(block.getLocation())) {
            autoCrafterManager.delete(block.getLocation());
            return;
        }
        AutoCrafterData data = new AutoCrafterData(block.getLocation());
        if (!data.isMachine()) {
            return;
        }
        Machine machine = autoCrafterManager.getMachineById().get(data.getId());
        if (!(machine instanceof AutoCrafterMachine)) {
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), true)) {
            return;
        }
        AutoCrafterMenu menu = new AutoCrafterMenu(autoCrafterManager, data);
        menu.open(player);
        e.setCancelled(true);
    }

    @EventHandler
    public void onMenuClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (!(e.getInventory().getHolder() instanceof AutoCrafterRecipeMenu)) {
            return;
        }
        AutoCrafterRecipeMenu menu = (AutoCrafterRecipeMenu) e.getInventory().getHolder();
        menu.onClose(player);
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        if (inv.getHolder() instanceof AutoCrafterMenu || inv.getHolder() instanceof AutoCrafterProgressMenu) {
            e.setCancelled(true);
            return;
        }
        if (!(inv.getHolder() instanceof AutoCrafterRecipeMenu)) {
            return;
        }
        AutoCrafterRecipeMenu menu = (AutoCrafterRecipeMenu) inv.getHolder();
        for (int slot : e.getRawSlots()) {
            ItemStack item = e.getNewItems().get(slot);
            menu.click(player, slot, item);
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof AutoCrafterMenu)) {
            return;
        }
        e.setCancelled(true);
        Integer tries = autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
        if (tries != null && tries >= 3) {
            MessageUtils editor = new MessageUtils("&cPlease slow down");
            editor.color();
            player.sendMessage(editor.getMessage());
            return;
        }
        AutoCrafterMenu menu = (AutoCrafterMenu) e.getClickedInventory().getHolder();
        ItemStack cursor = menu.click(player, e.getRawSlot(), e.getCursor());
        e.setCursor(cursor);
        if (autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId()) == null) {
            autoCrafterManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), 1);
        }else {
            int number = autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
            autoCrafterManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), number + 1);
        }
    }

    @EventHandler
    public void onProgressMenuClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof AutoCrafterProgressMenu)) {
            return;
        }
        e.setCancelled(true);
        Integer tries = autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
        if (tries != null && tries >= 3) {
            MessageUtils editor = new MessageUtils("&cPlease slow down");
            editor.color();
            player.sendMessage(editor.getMessage());
            return;
        }
        AutoCrafterProgressMenu menu = (AutoCrafterProgressMenu) e.getClickedInventory().getHolder();
        ItemStack cursor = menu.click(e.getRawSlot(), e.getCursor());
        e.setCursor(cursor);
        if (autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId()) == null) {
            autoCrafterManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), 1);
        }else {
            int number = autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
            autoCrafterManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), number + 1);
        }
    }

    @EventHandler
    public void onRecipeMenuClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof AutoCrafterRecipeMenu)) {
            return;
        }
        e.setCancelled(true);
        Integer tries = autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
        if (tries != null && tries >= 3) {
            MessageUtils editor = new MessageUtils("&cPlease slow down");
            editor.color();
            player.sendMessage(editor.getMessage());
            return;
        }
        AutoCrafterRecipeMenu menu = (AutoCrafterRecipeMenu) e.getClickedInventory().getHolder();
        ItemStack cursor = menu.click(player, e.getRawSlot(), e.getCursor());
        e.setCursor(cursor);
        if (autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId()) == null) {
            autoCrafterManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), 1);
        }else {
            int number = autoCrafterManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
            autoCrafterManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), number + 1);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        autoCrafterManager.getDataByPlayer().remove(e.getPlayer().getUniqueId());
    }
}
