package com.mortisdevelopment.mortismachines;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.mortisdevelopment.mortismachines.machines.MachineManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MortisMachines extends JavaPlugin {

    private static MortisMachines Instance;
    private ProtocolManager protocolManager;
    private boolean towny;
    private boolean hopper;
    private MachineManager manager;

    @Override
    public void onEnable() {
        // Plugin startup logic
        Instance = this;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.towny = getServer().getPluginManager().getPlugin("Towny") != null;
        this.hopper = getServer().getPluginManager().getPlugin("MortisHoppers") != null;
        this.manager = new MachineManager();
    }

    public static MortisMachines getInstance() {
        return Instance;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }

    public boolean hasTowny() {
        return towny;
    }

    public boolean hasHopper() {
        return hopper;
    }

    public MachineManager getManager() {
        return manager;
    }
}
