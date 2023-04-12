package me.none030.mortismachines.machines.sound;

import io.papermc.paper.event.player.AsyncChatEvent;
import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.data.MachineData;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MessageUtils;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class SoundListener implements Listener {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final SoundManager soundManager;

    public SoundListener(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    @EventHandler
    public void onMachineBuild(BlockPlaceEvent e) {
        if (e.isCancelled()) {
            return;
        }
        Player player = e.getPlayer();
        Block block = e.getBlockPlaced();
        for (Machine machine : soundManager.getMachines()) {
            if (!(machine instanceof SoundMachine)) {
                continue;
            }
            Structure structure = machine.getStructure(block.getLocation(), true);
            if (structure != null) {
                SoundData data = new SoundData(block.getLocation());
                data.create(machine.getId(), structure.getId());
                soundManager.add(block.getLocation());
                player.sendMessage(soundManager.getMessage("MACHINE_BUILD"));
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
        if (!soundManager.getCores().contains(block.getLocation())) {
            soundManager.delete(block.getLocation());
            return;
        }
        SoundData data = new SoundData(block.getLocation());
        if (!data.isMachine()) {
            return;
        }
        Machine machine = soundManager.getMachineById().get(data.getId());
        if (!(machine instanceof SoundMachine)) {
            return;
        }
        Structure structure = machine.getStructure(data.getStructureId());
        if (structure == null || !structure.isStructure(data.getCore(), true)) {
            return;
        }
        soundManager.getDataByPlayer().put(player.getUniqueId(), data);
        player.sendMessage(soundManager.getMessage("SET_MESSAGE"));
        long[] count = {0};
        new BukkitRunnable() {
            @Override
            public void run() {
                count[0] = count[0]++;
                if (soundManager.getDataByPlayer().get(player.getUniqueId()) == null) {
                    cancel();
                }
                if (count[0] >= 10) {
                    soundManager.getDataByPlayer().remove(player.getUniqueId());
                    player.sendMessage(soundManager.getMessage("SET_MESSAGE_EXPIRED"));
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 200L);
        e.setCancelled(true);
    }

    @EventHandler
    public void onMessageSet(AsyncChatEvent e) {
        Player player = e.getPlayer();
        String message = LegacyComponentSerializer.legacyAmpersand().serialize(e.message());
        MachineData data = soundManager.getDataByPlayer().get(player.getUniqueId());
        if (!(data instanceof SoundData) || !data.isMachine()) {
            return;
        }
        Machine machine = soundManager.getMachineById().get(data.getId());
        if (!(machine instanceof SoundMachine) || ((SoundMachine) machine).isInBlacklist(message)) {
            return;
        }
        e.setCancelled(true);
        soundManager.getDataByPlayer().remove(player.getUniqueId());
        MessageUtils editor = new MessageUtils(message);
        editor.color();
        ((SoundData) data).setMessage(editor.getMessage());
        player.sendMessage(soundManager.getMessage("MESSAGE_SET"));
    }
}
