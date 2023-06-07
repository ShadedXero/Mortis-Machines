package me.none030.mortismachines.machines;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import me.none030.mortismachines.MortisMachines;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

public class MachinePacketListener {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final ProtocolManager protocolManager = plugin.getProtocolManager();
    private final Manager manager;

    public MachinePacketListener(Manager manager) {
        this.manager = manager;
        listen();
    }

    private void listen() {
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.BLOCK_CHANGE) {
            @Override
            public void onPacketSending(PacketEvent e) {
                Player player = e.getPlayer();
                PacketContainer container = e.getPacket();
                BlockPosition position = container.getBlockPositionModifier().read(0);
                Location core = position.toLocation(player.getWorld()).getBlock().getLocation();
                if (!manager.getCores().contains(core)) {
                    return;
                }
                BlockData data = manager.getDataByCore().get(core);
                if (data == null) {
                    return;
                }
                container.getBlockData().write(0, WrappedBlockData.createData(data));
                e.setCancelled(true);
            }
        });
        protocolManager.addPacketListener(new PacketAdapter(plugin, PacketType.Play.Server.MULTI_BLOCK_CHANGE) {
            @Override
            public void onPacketSending(PacketEvent e) {
                Player player = e.getPlayer();
                PacketContainer container = e.getPacket();
                BlockPosition chunkCoords = container.getSectionPositions().read(0);
                WrappedBlockData[] dataList = container.getBlockDataArrays().read(0);
                short[] shorts = container.getShortArrays().read(0);
                for (int i = 0; i < shorts.length; i++) {
                    short number = shorts[i];
                    int x = getX(number) + chunkCoords.getX() * 16;
                    int y = getY(number) + chunkCoords.getY() * 16;
                    int z = getZ(number) + chunkCoords.getZ() * 16;
                    Location core = new Location(player.getWorld(), x, y, z);
                    if (!manager.getCores().contains(core)) {
                        continue;
                    }
                    BlockData data = manager.getDataByCore().get(core);
                    if (data == null) {
                        continue;
                    }
                    dataList[i].setType(data.getMaterial());
                }
                container.getBlockDataArrays().write(0, dataList);
            }
        });
    }

    private int getX(short number) {
        return number >>> 8 & 0xF;
    }

    private int getY(short number) {
        return number & 0xF;
    }

    private int getZ(short number) {
        return number >>> 4 & 0xF;
    }
}
