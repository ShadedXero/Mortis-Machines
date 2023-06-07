package me.none030.mortismachines.machines;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.config.ConfigManager;
import me.none030.mortismachines.items.ItemManager;
import me.none030.mortismachines.machines.autocrafter.AutoCrafterManager;
import me.none030.mortismachines.machines.idcard.IdCardManager;
import me.none030.mortismachines.machines.remotecontrol.RemoteControlManager;
import me.none030.mortismachines.machines.sound.SoundManager;
import me.none030.mortismachines.machines.timer.TimerManager;
import me.none030.mortismachines.recipes.RecipeManager;
import me.none030.mortismachines.structures.StructureManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class MachineManager {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private ItemManager itemManager;
    private RecipeManager recipeManager;
    private StructureManager structureManager;
    private AutoCrafterManager autoCrafterManager;
    private IdCardManager idCardManager;
    private RemoteControlManager remoteControlManager;
    private SoundManager soundManager;
    private TimerManager timerManager;
    private ConfigManager configManager;

    public MachineManager() {
        this.itemManager = new ItemManager();
        this.recipeManager = new RecipeManager();
        this.soundManager = new SoundManager();
        this.configManager = new ConfigManager(this);
        plugin.getServer().getPluginCommand("machine").setExecutor(new MachineCommand(this));
    }

    public void reload() {
        HandlerList.unregisterAll(plugin);
        Bukkit.getScheduler().cancelTasks(plugin);
        plugin.getProtocolManager().removePacketListeners(plugin);
        setItemManager(new ItemManager());
        setRecipeManager(new RecipeManager());
        setSoundManager(new SoundManager());
        setConfigManager(new ConfigManager(this));
    }

    public ItemManager getItemManager() {
        return itemManager;
    }

    public void setItemManager(ItemManager itemManager) {
        this.itemManager = itemManager;
    }

    public RecipeManager getRecipeManager() {
        return recipeManager;
    }

    public void setRecipeManager(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    public StructureManager getStructureManager() {
        return structureManager;
    }

    public void setStructureManager(StructureManager structureManager) {
        this.structureManager = structureManager;
    }

    public AutoCrafterManager getAutoCrafterManager() {
        return autoCrafterManager;
    }

    public void setAutoCrafterManager(AutoCrafterManager autoCrafterManager) {
        this.autoCrafterManager = autoCrafterManager;
    }

    public IdCardManager getIdCardManager() {
        return idCardManager;
    }

    public void setIdCardManager(IdCardManager idCardManager) {
        this.idCardManager = idCardManager;
    }

    public RemoteControlManager getRemoteControlManager() {
        return remoteControlManager;
    }

    public void setRemoteControlManager(RemoteControlManager remoteControlManager) {
        this.remoteControlManager = remoteControlManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public void setSoundManager(SoundManager soundManager) {
        this.soundManager = soundManager;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }

    public void setTimerManager(TimerManager timerManager) {
        this.timerManager = timerManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public void setConfigManager(ConfigManager configManager) {
        this.configManager = configManager;
    }
}
