package me.none030.mortismachines;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.none030.mortismachines.machines.MachineManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MortisMachines extends JavaPlugin {

    private static MortisMachines Instance;
    private ProtocolManager protocolManager;
    private MachineManager manager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Instance = this;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.manager = new MachineManager();
    }

    public static MortisMachines getInstance() {
        return Instance;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public MachineManager getManager() {
        return manager;
    }
}
