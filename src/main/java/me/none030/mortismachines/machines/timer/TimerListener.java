package me.none030.mortismachines.machines.timer;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.data.MachineData;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.machines.idcard.IdCardMenu;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MessageUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDateTime;

public class TimerListener implements Listener {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final TimerManager timerManager;

    public TimerListener(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    @EventHandler
    public void onMachineBuild(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player player = e.getPlayer();
        Location location = e.getBlockPlaced().getLocation();
        for (Machine machine : timerManager.getMachines()) {
            if (!(machine instanceof TimerMachine)) {
                continue;
            }
            Structure structure = machine.getStructure(location, true);
            if (structure != null) {
                TimerData data = new TimerData(location);
                TimerTime timerTime = new TimerTime(((TimerMachine) machine).getDefaultTime());
                data.create(structure.getId(), false, timerTime, ((TimerMachine) machine).getDefaultOutputTime());
                timerManager.add(location);
                player.sendMessage(timerManager.getMessage("MACHINE_BUILD"));
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
        if (!timerManager.getCores().contains(block.getLocation())) {
            timerManager.delete(block.getLocation());
            return;
        }
        TimerData data = new TimerData(block.getLocation());
        if (!data.isMachine()) {
            return;
        }
        Machine machine = timerManager.getMachineById().get(data.getId());
        if (!(machine instanceof TimerMachine)) {
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), false)) {
            return;
        }
        TimerMenu menu = new TimerMenu(timerManager, data);
        menu.open(player);
        e.setCancelled(true);
    }

    @EventHandler
    public void onTimeSet(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String message = LegacyComponentSerializer.legacyAmpersand().serialize(e.message());
        MachineData data = timerManager.getDataByPlayer().get(player.getUniqueId());
        if (!(data instanceof TimerData) || !data.isMachine()) {
            return;
        }
        e.setCancelled(true);
        timerManager.getDataByPlayer().remove(player.getUniqueId());
        MessageUtils utils = new MessageUtils(message);
        TimerTime time = utils.getTimerTime();
        if (time == null) {
            player.sendMessage(timerManager.getMessage("INVALID_TIME"));
            return;
        }
        if (!time.isLocal()) {
            if (time.getTime() < ((TimerData) data).getOutputTime()) {
                player.sendMessage(timerManager.getMessage("TIME_LESS_OUTPUT"));
                return;
            }
        }
        ((TimerData) data).setTime(time);
        ((TimerData) data).setLocalTime(LocalDateTime.now().getDayOfMonth());
        player.sendMessage(timerManager.getMessage("TIMER_SET"));
        new BukkitRunnable() {
            @Override
            public void run() {
                TimerMenu menu = new TimerMenu(timerManager, (TimerData) data);
                menu.open(player);
                Machine machine = timerManager.getMachineById().get(data.getId());
                if (!(machine instanceof TimerMachine)) {
                    return;
                }
                Structure structure = machine.getStructure(data.getStructureId());
                if (structure == null || !structure.isStructure(data.getCore(), false)) {
                    return;
                }
                ((TimerMachine) machine).activate(timerManager, (TimerData) data, time.isLocal());
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onOutputTimeSet(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String message = LegacyComponentSerializer.legacyAmpersand().serialize(e.message());
        TimerData data = timerManager.getOutputDataByPlayer().get(player.getUniqueId());
        if (data == null || !data.isMachine()) {
            return;
        }
        e.setCancelled(true);
        timerManager.getOutputDataByPlayer().remove(player.getUniqueId());
        MessageUtils utils = new MessageUtils(message);
        Long time = utils.getSeconds();
        if (time == null || time < -1) {
            player.sendMessage(timerManager.getMessage("INVALID_TIME"));
            return;
        }
        if (!data.getTime().isLocal()) {
            if (time > data.getTime().getTime()) {
                player.sendMessage(timerManager.getMessage("OUTPUT_GREATER_TIME"));
                return;
            }
        }
        data.setOutputTime(time);
        player.sendMessage(timerManager.getMessage("OUTPUT_TIMER_SET"));
        new BukkitRunnable() {
            @Override
            public void run() {
                TimerMenu menu = new TimerMenu(timerManager, data);
                menu.open(player);
                Machine machine = timerManager.getMachineById().get(data.getId());
                if (!(machine instanceof TimerMachine)) {
                    return;
                }
                Structure structure = machine.getStructure(data.getStructureId());
                if (structure == null || !structure.isStructure(data.getCore(), false)) {
                    return;
                }
                ((TimerMachine) machine).activate(timerManager, data, data.getTime().isLocal());
            }
        }.runTask(plugin);
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        if (e.getClickedInventory() == null || !(e.getClickedInventory().getHolder() instanceof TimerMenu)) {
            return;
        }
        e.setCancelled(true);
        Integer tries = timerManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
        if (tries != null && tries >= 3) {
            MessageUtils editor = new MessageUtils("&cPlease slow down");
            editor.color();
            player.sendMessage(editor.getMessage());
            return;
        }
        TimerMenu menu = (TimerMenu) e.getClickedInventory().getHolder();
        int slot = e.getRawSlot();
        menu.click(player, slot);
        if (timerManager.getPlayersInMenuCoolDown().get(player.getUniqueId()) == null) {
            timerManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), 1);
        }else {
            int number = timerManager.getPlayersInMenuCoolDown().get(player.getUniqueId());
            timerManager.getPlayersInMenuCoolDown().put(player.getUniqueId(), number + 1);
        }
    }

    @EventHandler
    public void onMenuDrag(InventoryDragEvent e) {
        Inventory inv = e.getInventory();
        if (inv.getHolder() instanceof TimerMenu) {
            e.setCancelled(true);
        }
    }
}
