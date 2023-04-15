package me.none030.mortismachines;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.palmergames.bukkit.towny.TownyAPI;
import me.none030.mortismachines.machines.MachineManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MortisMachines extends JavaPlugin {

    private static MortisMachines Instance;
    private ProtocolManager protocolManager;
    private boolean hasTowny;
    private MachineManager manager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Instance = this;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.hasTowny = getServer().getPluginManager().getPlugin("Towny") != null;
        this.manager = new MachineManager();
    }

    public static MortisMachines getInstance() {
        return Instance;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public boolean hasTowny() {
        return hasTowny;
    }

    public MachineManager getManager() {
        return manager;
    }
}
