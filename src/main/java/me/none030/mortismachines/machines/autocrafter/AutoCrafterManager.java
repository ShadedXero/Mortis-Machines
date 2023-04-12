package me.none030.mortismachines.machines.autocrafter;

import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.Machine;
import me.none030.mortismachines.machines.Manager;
import me.none030.mortismachines.machines.autocrafter.recipes.AutoCrafterRecipe;
import me.none030.mortismachines.menu.MenuItems;
import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MachineType;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoCrafterManager extends Manager {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final MenuItems menuItems;
    private final MenuItems progressMenuItems;
    private final MenuItems recipeMenuItems;

    public AutoCrafterManager(MenuItems menuItems, MenuItems progressMenuItems, MenuItems recipeMenuItems) {
        super(MachineType.AUTO_CRAFTER, false, false);
        this.menuItems = menuItems;
        this.progressMenuItems = progressMenuItems;
        this.recipeMenuItems = recipeMenuItems;
        plugin.getServer().getPluginManager().registerEvents(new AutoCrafterListener(this), plugin);
        check();
    }

    private void check() {
        new BukkitRunnable() {
            @Override
            public void run() {
                getPlayersInMenuCoolDown().clear();
                for (int i = 0; i < getCores().size(); i++) {
                    Location core = getCores().get(i);
                    if (core == null) {
                        continue;
                    }
                    AutoCrafterData data = new AutoCrafterData(core);
                    if (!data.isMachine()) {
                        delete(core);
                        continue;
                    }
                    Machine machine = getMachineById().get(data.getId());
                    if (!(machine instanceof AutoCrafterMachine)) {
                        delete(data);
                        continue;
                    }
                    Structure structure = machine.getStructure(data.getStructureId());
                    if (structure == null || !structure.isStructure(core, true)) {
                        delete(data);
                        continue;
                    }
                    if (data.isSetOffline()) {
                        data.setOnline(false);
                        continue;
                    }
                    NamespacedKey recipeKey = data.getRecipe();
                    if (recipeKey == null) {
                        data.setOnline(false);
                        continue;
                    }
                    AutoCrafterRecipe recipe = ((AutoCrafterMachine) machine).getRecipe(recipeKey);
                    if (recipe == null) {
                        data.setRecipe(null);
                        data.setOnline(false);
                        continue;
                    }
                    AutoCrafterFuel fuel = null;
                    if (((AutoCrafterMachine) machine).isRequireFuel()) {
                        ((AutoCrafterMachine) machine).checkFuel(data, structure);
                        ItemStack fuelItem = data.getFuel();
                        if (fuelItem == null) {
                            data.setOnline(false);
                            continue;
                        }
                        fuel = ((AutoCrafterMachine) machine).getFuel(fuelItem);
                        if (fuel == null) {
                            data.setOnline(false);
                            continue;
                        }
                        if (data.isManualMode()) {
                            data.setOnline(recipe.hasPower(fuel.getPower(fuelItem)));
                        } else {
                            data.setOnline(recipe.hasPower(fuel.getPower(fuelItem)) && structure.hasRedstoneSignal(core));
                        }
                    }else {
                        if (data.isManualMode()) {
                            data.setOnline(true);
                        } else {
                            data.setOnline(structure.hasRedstoneSignal(core));
                        }
                    }
                    if (!data.isOnline()) {
                        continue;
                    }
                    ((AutoCrafterMachine) machine).check(data, structure, recipe, fuel);
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void delete(AutoCrafterData data) {
        data.emptyGrid(data.getCore());
        data.emptyResult(data.getCore());
        data.emptyFuel(data.getCore());
        delete(data.getCore());
        data.delete();
    }

    public MenuItems getMenuItems() {
        return menuItems;
    }

    public MenuItems getProgressMenuItems() {
        return progressMenuItems;
    }

    public MenuItems getRecipeMenuItems() {
        return recipeMenuItems;
    }
}
