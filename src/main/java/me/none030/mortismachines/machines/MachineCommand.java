package me.none030.mortismachines.machines;

import me.none030.mortismachines.structures.Structure;
import me.none030.mortismachines.utils.MessageUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MachineCommand implements TabExecutor {

    private final MachineManager manager;

    public MachineCommand(MachineManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("mortismachines.reload")) {
                MessageUtils utils = new MessageUtils("&cYou do not have the permission to use this");
                utils.color();
                sender.sendMessage(utils.getMessage());
                return false;
            }
            manager.reload();
            MessageUtils utils = new MessageUtils("&cReloaded");
            utils.color();
            sender.sendMessage(utils.getMessage());
        }
        if (args[0].equalsIgnoreCase("recipe")) {
            if (!(sender instanceof Player)) {
                MessageUtils utils = new MessageUtils("&cThis command can only be executed by a player");
                utils.color();
                sender.sendMessage(utils.getMessage());
                return false;
            }
            Player player = (Player) sender;
            if (!player.hasPermission("mortismachines.recipe")) {
                MessageUtils utils = new MessageUtils("&cYou do not have the permission to use this");
                utils.color();
                player.sendMessage(utils.getMessage());
                return false;
            }
            ItemStack result = player.getInventory().getItemInMainHand();
            if (result.getType().equals(Material.AIR)) {
                MessageUtils utils = new MessageUtils("&cPlease hold a item in your hand");
                utils.color();
                player.sendMessage(utils.getMessage());
                return false;
            }
            List<Recipe> recipes = Bukkit.getRecipesFor(result);
            if (recipes.size() == 0) {
                MessageUtils utils = new MessageUtils("&cCould not find any recipes with that result");
                utils.color();
                player.sendMessage(utils.getMessage());
                return false;
            }
            for (Recipe recipe : recipes) {
                if (recipe instanceof ShapedRecipe) {
                    ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
                    MessageUtils utils = new MessageUtils("&c" + shapedRecipe.getKey());
                    utils.color();
                    player.sendMessage(utils.getMessage());
                }
                if (recipe instanceof ShapelessRecipe) {
                    ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
                    MessageUtils utils = new MessageUtils("&c" + shapelessRecipe.getKey());
                    utils.color();
                    player.sendMessage(utils.getMessage());
                }
            }
        }
        if (args[0].equalsIgnoreCase("structure")) {
            if (args[1].equalsIgnoreCase("save")) {
                if (!sender.hasPermission("mortismachines.structure.save")) {
                    MessageUtils utils = new MessageUtils("&cYou do not have the permission to use this");
                    utils.color();
                    sender.sendMessage(utils.getMessage());
                    return false;
                }
                String structureId = args[2];
                World world = Bukkit.getWorld(args[3]);
                if (world == null) {
                    MessageUtils utils = new MessageUtils("&cCould not save the structure");
                    utils.color();
                    sender.sendMessage(utils.getMessage());
                    return false;
                }
                double x;
                double y;
                double z;
                try {
                    x = Double.parseDouble(args[4]);
                    y = Double.parseDouble(args[5]);
                    z = Double.parseDouble(args[6]);
                } catch (NumberFormatException exp) {
                    MessageUtils utils = new MessageUtils("&cCould not save the structure");
                    utils.color();
                    sender.sendMessage(utils.getMessage());
                    return false;
                }
                boolean strict = Boolean.parseBoolean(args[7]);
                Location center = new Location(world, x, y, z);
                Structure structure = manager.getStructureManager().getStructure(center, structureId, strict);
                manager.getStructureManager().saveStructure(structure);
                manager.getConfigManager().getStructureConfig().saveStructure(structure);
                MessageUtils utils = new MessageUtils("&cThe structure has been saved");
                utils.color();
                sender.sendMessage(utils.getMessage());
            }
            if (args[1].equalsIgnoreCase("delete")) {
                if (!sender.hasPermission("mortismachines.structure.delete")) {
                    MessageUtils utils = new MessageUtils("&cYou do not have the permission to use this");
                    utils.color();
                    sender.sendMessage(utils.getMessage());
                    return false;
                }
                String structureId = args[2];
                manager.getConfigManager().getStructureConfig().deleteStructure(structureId);
                manager.getStructureManager().deleteStructure(structureId);
                MessageUtils utils = new MessageUtils("&cThe structure has been deleted");
                utils.color();
                sender.sendMessage(utils.getMessage());
            }
        }
        if (args[0].equalsIgnoreCase("give")) {
            if (!sender.hasPermission("mortismachines.give")) {
                MessageUtils utils = new MessageUtils("&cYou do not have the permission to use this");
                utils.color();
                sender.sendMessage(utils.getMessage());
                return false;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                MessageUtils utils = new MessageUtils("&cPlease enter a valid target");
                utils.color();
                sender.sendMessage(utils.getMessage());
                return false;
            }
            ItemStack item = manager.getItemManager().getItem(args[2]);
            if (item == null) {
                MessageUtils utils = new MessageUtils("&cPlease enter a valid item id");
                utils.color();
                sender.sendMessage(utils.getMessage());
                return false;
            }
            int amount = 1;
            if (args.length >= 4) {
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException ignored) {
                }
                if (amount > 64) {
                    amount = 64;
                }
            }
            item.setAmount(amount);
            if (target.getInventory().firstEmpty() != -1) {
                target.getInventory().addItem(item);
            }else {
                target.getWorld().dropItemNaturally(target.getLocation(), item);
            }
            MessageUtils utils = new MessageUtils("&cItem Given");
            utils.color();
            sender.sendMessage(utils.getMessage());
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> arguments = new ArrayList<>();
            arguments.add("structure");
            arguments.add("give");
            arguments.add("reload");
            arguments.add("recipe");
            return arguments;
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("structure")) {
                List<String> arguments = new ArrayList<>();
                arguments.add("save");
                arguments.add("delete");
                return arguments;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("give")) {
                return new ArrayList<>(manager.getItemManager().getItemById().keySet());
            }
            if (args[0].equalsIgnoreCase("structure") && args[1].equalsIgnoreCase("delete")) {
                return new ArrayList<>(manager.getStructureManager().getStructureById().keySet());
            }
        }
        return null;
    }
}
