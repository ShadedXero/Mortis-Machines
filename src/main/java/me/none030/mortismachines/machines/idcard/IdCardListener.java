package me.none030.mortismachines.machines.idcard;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.data.MachineData;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MessageUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class IdCardListener implements Listener {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final IdCardManager idCardManager;

    public IdCardListener(IdCardManager idCardManager) {
        this.idCardManager = idCardManager;
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
        for (Machine machine : idCardManager.getMachines()) {
            if (!(machine instanceof IdCardMachine)) {
                continue;
            }
            Structure structure = machine.getStructure(location, true);
            if (structure != null) {
                IdCardData data = new IdCardData(location);
                data.create(machine.getId(), structure.getId(), false, 10, player.getUniqueId().toString());
                idCardManager.add(location);
                ((IdCardMachine) machine).activate(idCardManager, data);
                player.sendMessage(idCardManager.getMessage("MACHINE_BUILD"));
                return;
            }
        }
    }

    @EventHandler
    public void onMachineInteract(PlayerInteractEvent e) {
        if (e.getHand() == null || !e.getHand().equals(EquipmentSlot.HAND)) {
            return;
        }
        Player player = e.getPlayer();
        Block block = e.getClickedBlock();
        if (block == null) {
            return;
        }
        if (!e.getAction().isRightClick() || player.isSneaking()) {
            return;
        }
        if (!idCardManager.getCores().contains(block.getLocation())) {
            idCardManager.delete(block.getLocation());
            return;
        }
        IdCardData data = new IdCardData(block.getLocation());
        if (!data.isMachine()) {
            return;
        }
        Machine machine = idCardManager.getMachineById().get(data.getId());
        if (!(machine instanceof IdCardMachine)) {
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), false)) {
            return;
        }
        if (!player.hasPermission("mortismachines.access")) {
            if (e.useInteractedBlock().equals(Event.Result.DENY)) {
                return;
            }
            if (plugin.hasTowny()) {
                if (!PlayerCacheUtil.getCachePermission(player, block.getLocation(), block.getType(), TownyPermission.ActionType.SWITCH)) {
                    player.sendMessage(idCardManager.getMessage("SWITCH"));
                    return;
                }
            }
        }else {
            e.setCancelled(false);
        }
        IdCardMenu menu = new IdCardMenu(idCardManager, data);
        menu.open(player);
        e.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        idCardManager.getDataByPlayer().remove(e.getPlayer().getUniqueId());
        idCardManager.getAddEditorByPlayer().remove(e.getPlayer().getUniqueId());
        idCardManager.getRemoveEditorByPlayer().remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onTimeSet(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String message = LegacyComponentSerializer.legacyAmpersand().serialize(e.message());
        MachineData data = idCardManager.getDataByPlayer().get(player.getUniqueId());
        if (!(data instanceof IdCardData) || !data.isMachine()) {
            return;
        }
        e.setCancelled(true);
        idCardManager.getDataByPlayer().remove(player.getUniqueId());
        MessageUtils utils = new MessageUtils(message);
        Long time = utils.getSeconds();
        if (time == null || time < 0 || time > 86400) {
            player.sendMessage(idCardManager.getMessage("INVALID_TIME"));
            return;
        }
        ((IdCardData) data).setTime(time);
        player.sendMessage(idCardManager.getMessage("TIMER_SET"));
        new BukkitRunnable() {
            @Override
            public void run() {
                IdCardMenu menu = new IdCardMenu(idCardManager,(IdCardData) data);
                menu.open(player);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onEditorAdd(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String message = LegacyComponentSerializer.legacyAmpersand().serialize(e.message());
        IdCardData data = idCardManager.getAddEditorByPlayer().get(player.getUniqueId());
        if (data == null || !data.isMachine()) {
            return;
        }
        e.setCancelled(true);
        idCardManager.getAddEditorByPlayer().remove(player.getUniqueId());
        OfflinePlayer target = Bukkit.getOfflinePlayer(message);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(idCardManager.getMessage("INVALID_TARGET"));
            return;
        }
        if (data.isEditorLimit()) {
            return;
        }
        data.addEditor(target);
        player.sendMessage(idCardManager.getMessage("EDITOR_ADDED"));
        new BukkitRunnable() {
            @Override
            public void run() {
                IdCardMenu menu = new IdCardMenu(idCardManager, data);
                menu.open(player);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onEditorRemove(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String message = LegacyComponentSerializer.legacyAmpersand().serialize(e.message());
        IdCardData data = idCardManager.getRemoveEditorByPlayer().get(player.getUniqueId());
        if (data == null || !data.isMachine()) {
            return;
        }
        e.setCancelled(true);
        idCardManager.getRemoveEditorByPlayer().remove(player.getUniqueId());
        OfflinePlayer target = Bukkit.getOfflinePlayer(message);
        if (!target.hasPlayedBefore()) {
            player.sendMessage(idCardManager.getMessage("INVALID_TARGET"));
            return;
        }
        data.removeEditor(target);
        player.sendMessage(idCardManager.getMessage("EDITOR_REMOVED"));
        new BukkitRunnable() {
            @Override
            public void run() {
                IdCardMenu menu = new IdCardMenu(idCardManager, data);
                menu.open(player);
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        Inventory inv = e.getInventory();
        Inventory clickedInv = e.getClickedInventory();
        if (inv.getHolder() instanceof IdCardMenu) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
                return;
            }
        }
        if (clickedInv != null && clickedInv.getHolder() instanceof IdCardMenu) {
            if (e.isShiftClick()) {
                e.setCancelled(true);
                return;
            }
        }else {
            return;
        }
        e.setCancelled(true);
        Integer tries = idCardManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
        if (tries != null && tries >= 3) {
            MessageUtils editor = new MessageUtils("&cPlease slow down");
            editor.color();
            player.sendMessage(editor.getMessage());
            return;
        }
        IdCardMenu menu = (IdCardMenu) clickedInv.getHolder();
        int slot = e.getRawSlot();
        ItemStack cursor = e.getCursor();
        ItemStack item = menu.click(player, slot, cursor);
        player.setItemOnCursor(item);
        if (idCardManager.getPlayersInMenuCoolDown().get(player.getUniqueId()) == null) {
            idCardManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), 1);
        }else {
            int number = idCardManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
            idCardManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), number + 1);
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getHolder() instanceof IdCardMenu) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onMachineBreak(BlockBreakEvent e) {
        Player player = e.getPlayer();
        if (!e.isDropItems()) {
            return;
        }
        if (!player.hasPermission("mortismachines.access")) {
            if (e.isCancelled()) {
                return;
            }
        }else {
            e.setCancelled(false);
        }
        Location loc = e.getBlock().getLocation();
        for (Location location : idCardManager.getCores()) {
            if (location.equals(loc)) {
                IdCardData data = new IdCardData(location);
                if (!data.isMachine()) {
                    return;
                }
                Machine machine = idCardManager.getMachineById().get(data.getId());
                if (machine == null) {
                    return;
                }
                Structure structure = machine.getStructure(data.getStructureId());
                if (structure == null) {
                    return;
                }
                idCardManager.delete(data, structure.getCore().getData());
                return;
            }
        }
    }

    @EventHandler
    public void onMachineExplode(BlockExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        for (Location location : idCardManager.getCores()) {
            for (Block block : e.blockList()) {
                Location loc = block.getLocation();
                if (location.equals(loc)) {
                    IdCardData data = new IdCardData(location);
                    if (!data.isMachine()) {
                        return;
                    }
                    Machine machine = idCardManager.getMachineById().get(data.getId());
                    if (machine == null) {
                        return;
                    }
                    Structure structure = machine.getStructure(data.getStructureId());
                    if (structure == null) {
                        return;
                    }
                    idCardManager.delete(data, structure.getCore().getData());
                }
            }
        }
    }

    @EventHandler
    public void onEntityExplodeMachine(EntityExplodeEvent e) {
        if (e.isCancelled()) {
            return;
        }
        for (Location location : idCardManager.getCores()) {
            for (Block block : e.blockList()) {
                Location loc = block.getLocation();
                if (location.equals(loc)) {
                    IdCardData data = new IdCardData(location);
                    if (!data.isMachine()) {
                        return;
                    }
                    Machine machine = idCardManager.getMachineById().get(data.getId());
                    if (machine == null) {
                        return;
                    }
                    Structure structure = machine.getStructure(data.getStructureId());
                    if (structure == null) {
                        return;
                    }
                    idCardManager.delete(location, structure.getCore().getData());
                }
            }
        }
    }

    @EventHandler
    public void onMachinePhysics(BlockPhysicsEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Block block = e.getBlock();
        Location loc = block.getLocation();
        for (Location location : idCardManager.getCores()) {
            if (location.equals(loc)) {
                IdCardData data = new IdCardData(location);
                if (!data.isMachine()) {
                    return;
                }
                Machine machine = idCardManager.getMachineById().get(data.getId());
                if (machine == null) {
                    return;
                }
                Structure structure = machine.getStructure(data.getStructureId());
                if (structure == null) {
                    return;
                }
                if (!block.getRelative(0, -1, 0).getType().equals(Material.AIR)) {
                    return;
                }
                idCardManager.delete(location, structure.getCore().getData());
            }
        }
    }
}
