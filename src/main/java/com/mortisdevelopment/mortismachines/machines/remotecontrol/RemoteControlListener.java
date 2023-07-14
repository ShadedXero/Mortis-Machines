package com.mortisdevelopment.mortismachines.machines.remotecontrol;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.machines.Machine;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.remotes.ClickerRemote;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.remotes.DeadmanRemote;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.remotes.FlipFlopRemote;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.remotes.Remote;
import com.mortisdevelopment.mortismachines.utils.MessageUtils;
import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import com.mortisdevelopment.mortismachines.structures.Structure;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class RemoteControlListener implements Listener {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final RemoteControlManager remoteControlManager;

    public RemoteControlListener(RemoteControlManager remoteControlManager) {
        this.remoteControlManager = remoteControlManager;
    }

    @EventHandler
    public void onMachineBuild(BlockPlaceEvent e) {
        Player player = e.getPlayer();
        if (!player.hasPermission("mortismachines.access")) {
            if (e.isCancelled()) {
                return;
            }
        }else {
            e.setCancelled(false);
        }
        Location location = e.getBlockPlaced().getLocation();
        for (Machine machine : remoteControlManager.getMachines()) {
            if (!(machine instanceof RemoteControlMachine)) {
                continue;
            }
            Structure structure = machine.getStructure(location, true);
            if (structure != null) {
                RemoteControlData data = new RemoteControlData(location);
                data.create(machine.getId(), structure.getId(), false);
                remoteControlManager.add(location);
                player.sendMessage(remoteControlManager.getMessage("MACHINE_BUILD"));
                return;
            }
        }
    }

    @EventHandler
    public void onMachineInteract(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!e.getAction().isRightClick() || player.isSneaking()) {
            return;
        }
        if (!remoteControlManager.getCores().contains(block.getLocation())) {
            remoteControlManager.delete(block.getLocation());
            return;
        }
        RemoteControlData data = new RemoteControlData(block.getLocation());
        if (!data.isMachine()) {
            return;
        }
        Machine machine = remoteControlManager.getMachineById().get(data.getId());
        if (!(machine instanceof RemoteControlMachine)) {
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), false)) {
            return;
        }
        if (e.getHand() == null || !e.getHand().equals(EquipmentSlot.HAND)) {
            e.setCancelled(true);
            return;
        }
        if (!player.hasPermission("mortismachines.access")) {
            if (e.useInteractedBlock().equals(Event.Result.DENY)) {
                return;
            }
            if (plugin.hasTowny()) {
                if (!PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.SWITCH)) {
                    player.sendMessage(remoteControlManager.getMessage("SWITCH"));
                    return;
                }
            }
        }
        e.setCancelled(true);
        RemoteControlMenu menu = new RemoteControlMenu(remoteControlManager, data);
        menu.open(player);
    }

    @EventHandler
    public void onDetonatorInteract(PlayerInteractEvent e) {
        if (e.useItemInHand().equals(Event.Result.DENY) || !e.getAction().isRightClick()) {
            return;
        }
        Player player = e.getPlayer();
        ItemStack item = e.getItem();
        if (item == null || item.getType().equals(Material.AIR)) {
            return;
        }
        Location core = getCore(item);
        if (core == null) {
            return;
        }
        if (!remoteControlManager.getCores().contains(core)) {
            remoteControlManager.delete(core);
            return;
        }
        RemoteControlData data = new RemoteControlData(core);
        if (!data.isMachine()) {
            deleteDetonator(item);
            return;
        }
        Machine machine = remoteControlManager.getMachineById().get(data.getId());
        if (!(machine instanceof RemoteControlMachine)) {
            deleteDetonator(item);
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), false)) {
            deleteDetonator(item);
            return;
        }
        String id = getId(item);
        Remote remote = ((RemoteControlMachine) machine).getRemote(id);
        if (remote == null || !data.isDetonator(item)) {
            deleteDetonator(item);
            return;
        }
        e.setCancelled(true);
        if (!remote.isInRange(player.getUniqueId(), core)) {
            player.sendMessage(remoteControlManager.getMessage("NOT_IN_RANGE"));
            return;
        }
        if (remote instanceof FlipFlopRemote) {
            ((FlipFlopRemote) remote).activate((RemoteControlMachine) machine, data);
            player.sendMessage(remoteControlManager.getMessage("REMOTE_USE"));
        }
        if (remote instanceof ClickerRemote) {
            ((ClickerRemote) remote).activate((RemoteControlMachine) machine, data);
            player.sendMessage(remoteControlManager.getMessage("REMOTE_USE"));
        }
        if (remote instanceof DeadmanRemote) {
            if (isArmed(item)) {
                setArmed(item, false);
                player.sendMessage(remoteControlManager.getMessage("DETONATOR_UNARMED"));
            }else {
                setArmed(item, true);
                player.sendMessage(remoteControlManager.getMessage("DETONATOR_ARMED"));
            }
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        Player player = e.getPlayer();
        ItemStack item = e.getItemDrop().getItemStack();
        Location core = getCore(item);
        if (core == null) {
            return;
        }
        if (!remoteControlManager.getCores().contains(core)) {
            remoteControlManager.delete(core);
            return;
        }
        RemoteControlData data = new RemoteControlData(core);
        if (!data.isMachine()) {
            deleteDetonator(item);
            return;
        }
        Machine machine = remoteControlManager.getMachineById().get(data.getId());
        if (!(machine instanceof RemoteControlMachine)) {
            deleteDetonator(item);
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), false)) {
            deleteDetonator(item);
            return;
        }
        String id = getId(item);
        Remote remote = ((RemoteControlMachine) machine).getRemote(id);
        if (!data.isDetonator(item)) {
            deleteDetonator(item);
            return;
        }
        if (!(remote instanceof DeadmanRemote)) {
            return;
        }
        if (!remote.isInRange(player.getUniqueId(), core)) {
            player.sendMessage(remoteControlManager.getMessage("NOT_IN_RANGE"));
            return;
        }
        if (!isArmed(item)) {
            return;
        }
        ((DeadmanRemote) remote).activate((RemoteControlMachine) machine, data);
        setArmed(item, false);
        player.sendMessage(remoteControlManager.getMessage("REMOTE_USE"));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Location core = getCore(item);
        if (core == null) {
            return;
        }
        if (!remoteControlManager.getCores().contains(core)) {
            remoteControlManager.delete(core);
            return;
        }
        RemoteControlData data = new RemoteControlData(core);
        if (!data.isMachine()) {
            deleteDetonator(item);
            return;
        }
        Machine machine = remoteControlManager.getMachineById().get(data.getId());
        if (!(machine instanceof RemoteControlMachine)) {
            deleteDetonator(item);
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), false)) {
            deleteDetonator(item);
            return;
        }
        String id = getId(item);
        Remote remote = ((RemoteControlMachine) machine).getRemote(id);
        if (!data.isDetonator(item)) {
            deleteDetonator(item);
            return;
        }
        if (!(remote instanceof DeadmanRemote)) {
            return;
        }
        if (!remote.isInRange(player.getUniqueId(), core)) {
            player.sendMessage(remoteControlManager.getMessage("NOT_IN_RANGE"));
            return;
        }
        if (!isArmed(item)) {
            return;
        }
        ((DeadmanRemote) remote).activate((RemoteControlMachine) machine, data);
        setArmed(item, false);
        player.sendMessage(remoteControlManager.getMessage("REMOTE_USE"));
    }

    @EventHandler
    public void onHotBarSwitch(PlayerItemHeldEvent e) {
        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItem(e.getPreviousSlot());
        if (item == null) {
            return;
        }
        Location core = getCore(item);
        if (core == null) {
            return;
        }
        if (!remoteControlManager.getCores().contains(core)) {
            remoteControlManager.delete(core);
            return;
        }
        RemoteControlData data = new RemoteControlData(core);
        if (!data.isMachine()) {
            deleteDetonator(item);
            return;
        }
        Machine machine = remoteControlManager.getMachineById().get(data.getId());
        if (!(machine instanceof RemoteControlMachine)) {
            deleteDetonator(item);
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), false)) {
            deleteDetonator(item);
            return;
        }
        String id = getId(item);
        Remote remote = ((RemoteControlMachine) machine).getRemote(id);
        if (!data.isDetonator(item)) {
            deleteDetonator(item);
            return;
        }
        if (!(remote instanceof DeadmanRemote)) {
            return;
        }
        if (!remote.isInRange(player.getUniqueId(), core)) {
            player.sendMessage(remoteControlManager.getMessage("NOT_IN_RANGE"));
            return;
        }
        if (!isArmed(item)) {
            return;
        }
        ((DeadmanRemote) remote).activate((RemoteControlMachine) machine, data);
        setArmed(item, false);
        player.sendMessage(remoteControlManager.getMessage("REMOTE_USE"));
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null) {
            return;
        }
        Location core = getCore(item);
        if (core == null) {
            return;
        }
        if (!remoteControlManager.getCores().contains(core)) {
            remoteControlManager.delete(core);
            return;
        }
        RemoteControlData data = new RemoteControlData(core);
        if (!data.isMachine()) {
            deleteDetonator(item);
            return;
        }
        Machine machine = remoteControlManager.getMachineById().get(data.getId());
        if (!(machine instanceof RemoteControlMachine)) {
            deleteDetonator(item);
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), false)) {
            deleteDetonator(item);
            return;
        }
        String id = getId(item);
        Remote remote = ((RemoteControlMachine) machine).getRemote(id);
        if (!data.isDetonator(item)) {
            deleteDetonator(item);
            return;
        }
        if (!(remote instanceof DeadmanRemote)) {
            return;
        }
        if (!remote.isInRange(player.getUniqueId(), core)) {
            player.sendMessage(remoteControlManager.getMessage("NOT_IN_RANGE"));
            return;
        }
        if (!isArmed(item)) {
            return;
        }
        ((DeadmanRemote) remote).activate((RemoteControlMachine) machine, data);
        setArmed(item, false);
        player.sendMessage(remoteControlManager.getMessage("REMOTE_USE"));
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        Inventory clickedInv = e.getClickedInventory();
        if (inv.getHolder() instanceof RemoteControlMenu) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
                return;
            }
        }
        if (clickedInv != null && clickedInv.getHolder() instanceof RemoteControlMenu) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
                return;
            }
        }else {
            return;
        }
        e.setCancelled(true);
        Integer tries = remoteControlManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
        if (tries != null && tries >= 3) {
            MessageUtils editor = new MessageUtils("&cPlease slow down");
            editor.color();
            player.sendMessage(editor.getMessage());
            return;
        }
        RemoteControlMenu menu = (RemoteControlMenu) clickedInv.getHolder();
        int slot = e.getRawSlot();
        ItemStack cursor = e.getCursor();
        menu.click(player, slot, cursor);
        if (remoteControlManager.getPlayersInMenuCoolDown().get(player.getUniqueId()) == null) {
            remoteControlManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), 1);
        }else {
            int number = remoteControlManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
            remoteControlManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), number + 1);
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getHolder() instanceof RemoteControlMenu) {
            e.setCancelled(true);
        }
    }

    private void deleteDetonator(ItemStack item) {
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "MortisMachinesId"));
        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "MortisMachinesCore"));
        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "MortisMachinesDetonator"));
        meta.getPersistentDataContainer().remove(new NamespacedKey(plugin, "MortisMachinesArmed"));
        item.setItemMeta(meta);
    }

    private String getId(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        return meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "MortisMachinesId"), PersistentDataType.STRING);
    }

    private Location getCore(ItemStack item) {
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }
        String raw = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "MortisMachinesCore"), PersistentDataType.STRING);
        if (raw == null) {
            return null;
        }
        String[] rawCore = raw.split(",");
        World world = Bukkit.getWorld(rawCore[0]);
        if (world == null) {
            return null;
        }
        return new Location(world, Double.parseDouble(rawCore[1]), Double.parseDouble(rawCore[2]), Double.parseDouble(rawCore[3]));
    }

    private boolean isArmed(ItemStack item) {
        if (item == null) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }
        String raw = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "MortisMachinesArmed"), PersistentDataType.STRING);
        if (raw == null) {
            return false;
        }
        return Boolean.parseBoolean(raw);
    }

    private void setArmed(ItemStack item, boolean armed) {
        if (item == null) {
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "MortisMachinesArmed"), PersistentDataType.STRING, Boolean.toString(armed));
        item.setItemMeta(meta);
    }
}
