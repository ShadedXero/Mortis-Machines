package com.mortisdevelopment.mortismachines.machines;

import com.mortisdevelopment.mortismachines.MortisMachines;
import com.mortisdevelopment.mortismachines.config.ConfigManager;
import com.mortisdevelopment.mortismachines.data.DataManager;
import com.mortisdevelopment.mortismachines.items.ItemManager;
import com.mortisdevelopment.mortismachines.machines.autocrafter.AutoCrafterManager;
import com.mortisdevelopment.mortismachines.machines.idcard.IdCardManager;
import com.mortisdevelopment.mortismachines.machines.remotecontrol.RemoteControlManager;
import com.mortisdevelopment.mortismachines.machines.sound.SoundManager;
import com.mortisdevelopment.mortismachines.recipes.RecipeManager;
import com.mortisdevelopment.mortismachines.structures.StructureManager;
import com.mortisdevelopment.mortismachines.machines.timer.TimerManager;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;

public class MachineManager {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private ItemManager itemManager;
    private RecipeManager recipeManager;
    private StructureManager structureManager;
    private DataManager dataManager;
    private AutoCrafterManager autoCrafterManager;
    private IdCardManager idCardManager;
    private RemoteControlManager remoteControlManager;
    private SoundManager soundManager;
    private TimerManager timerManager;
    private ConfigManager configManager;

    public MachineManager() {
        this.itemManager = new ItemManager();
        this.recipeManager = new RecipeManager();
        this.configManager = new ConfigManager(this);
        plugin.getServer().getPluginCommand("machine").setExecutor(new MachineCommand(this));
    }

    public void reload() {
        HandlerList.unregisterAll(plugin);
        Bukkit.getScheduler().cancelTasks(plugin);
        plugin.getProtocolManager().removePacketListeners(plugin);
        setItemManager(new ItemManager());
        setRecipeManager(new RecipeManager());
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

    public DataManager getDataManager() {
        return dataManager;
    }

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
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
