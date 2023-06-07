package me.none030.mortismachines.machines.autocrafter.recipes;

import me.none030.mortishoppers.data.HopperData;
import me.none030.mortishoppers.utils.HopperMode;
import me.none030.mortismachines.MortisMachines;
import me.none030.mortismachines.machines.autocrafter.AutoCrafterData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;

import java.util.List;
import java.util.Map;

public abstract class AutoCrafterRecipe {

    private final MortisMachines plugin = MortisMachines.getInstance();
    private final NamespacedKey key;
    private final ItemStack[] ingredients;
    private final ItemStack result;
    private final long time;
    private final long power;
    private final int size;

    public AutoCrafterRecipe(NamespacedKey key, ItemStack[] ingredients, ItemStack result, long time, long power, int size) {
        this.size = size;
        this.key = key;
        this.ingredients = ingredients;
        this.result = result;
        this.time = time;
        this.power = power;
    }

    public AutoCrafterRecipe(Recipe recipe, long time, long power, int size) {
        this.size = size;
        this.key = getKey(recipe);
        this.ingredients = getGrid(recipe);
        this.result = recipe.getResult();
        this.time = time;
        this.power = power;
    }

    public boolean hasPower(long power) {
        return power >= this.power;
    }

    public ItemStack[] getEmptyGrid() {
        ItemStack[] grid = new ItemStack[size];
        for (int i = 0; i < grid.length; i++) {
            grid[i] = new ItemStack(Material.AIR);
        }
        return grid;
    }

    public boolean isRecipe(ItemStack[] ingredients) {
        if (ingredients == null) {
            return false;
        }
        for (int i = 0; i < getSize(); i++) {
            ItemStack ingredient = this.ingredients[i];
            int amount = ingredient.getAmount();
            if (ingredients[i].getType().equals(Material.AIR) && ingredient.getType().equals(Material.AIR)) {
                continue;
            }
            if (!ingredients[i].isSimilar(ingredient)) {
                return false;
            }
            if (ingredients[i].getAmount() < amount) {
                return false;
            }
        }
        return true;
    }

    public void craft(AutoCrafterData data) {
        if (!isRecipe(data.getGrid())) {
            return;
        }
        ItemStack[] grid = data.getGrid();
        if (grid == null) {
            return;
        }
        for (int i = 0; i < getSize(); i++) {
            ItemStack ingredient = getIngredients()[i];
            int amount = ingredient.getAmount();
            ItemStack gridItem = grid[i];
            gridItem.setAmount(gridItem.getAmount() - amount);
            grid[i] = gridItem;
        }
        data.setGrid(grid);
    }

    public NamespacedKey getKey(Recipe recipe) {
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            return shapedRecipe.getKey();
        }
        if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            return shapelessRecipe.getKey();
        }
        return null;
    }

    public boolean isKey(NamespacedKey key) {
        return this.key.equals(key);
    }

    public ItemStack[] getGrid(Recipe recipe) {
        ItemStack[] grid = getEmptyGrid();
        if (recipe instanceof ShapedRecipe) {
            ShapedRecipe shapedRecipe = (ShapedRecipe) recipe;
            Map<Character, RecipeChoice> recipeChoiceByChar = shapedRecipe.getChoiceMap();
            int index = 0;
            for (String characters : shapedRecipe.getShape()) {
                for (char character : characters.toCharArray()) {
                    RecipeChoice recipeChoice = recipeChoiceByChar.get(character);
                    if (recipeChoice instanceof RecipeChoice.MaterialChoice) {
                        RecipeChoice.MaterialChoice choice = (RecipeChoice.MaterialChoice) recipeChoice;
                        grid[index] = choice.getItemStack();
                    }
                    if (recipeChoice instanceof RecipeChoice.ExactChoice) {
                        RecipeChoice.ExactChoice choice = (RecipeChoice.ExactChoice) recipeChoice;
                        grid[index] = choice.getItemStack();
                    }
                    index++;
                }
            }
        }
        if (recipe instanceof ShapelessRecipe) {
            ShapelessRecipe shapelessRecipe = (ShapelessRecipe) recipe;
            List<RecipeChoice> recipeChoices = shapelessRecipe.getChoiceList();
            for (int i = 0; i < grid.length; i++) {
                if (i >= recipeChoices.size()) {
                    grid[i] = new ItemStack(Material.AIR, 1);
                    continue;
                }
                RecipeChoice recipeChoice = recipeChoices.get(i);
                if (recipeChoice instanceof RecipeChoice.MaterialChoice) {
                    RecipeChoice.MaterialChoice choice = (RecipeChoice.MaterialChoice) recipeChoice;
                    grid[i] = choice.getItemStack();
                }
                if (recipeChoice instanceof RecipeChoice.ExactChoice) {
                    RecipeChoice.ExactChoice choice = (RecipeChoice.ExactChoice) recipeChoice;
                    grid[i] = choice.getItemStack();
                }
            }
        }
        return grid;
    }

    public void addItem(int index, ItemStack item, AutoCrafterData data) {
        if (item == null || item.getType().isAir()) {
            return;
        }
        ItemStack[] grid = data.getGrid();
        if (grid == null) {
            return;
        }
        ItemStack ingredient = ingredients[index];
        if (ingredient == null || ingredient.getType().equals(Material.AIR) || !item.isSimilar(ingredient)) {
            return;
        }
        ItemStack gridItem = grid[index];
        if (gridItem == null || gridItem.getType().equals(Material.AIR)) {
            grid[index] = item.clone();
            item.setAmount(0);
        } else {
            if (isFull(gridItem)) {
                return;
            }
            int itemAmount = item.getAmount();
            int gridItemAmount = gridItem.getAmount();
            int maxAmount = gridItem.getMaxStackSize();
            int space = maxAmount - gridItemAmount;
            if (itemAmount > space) {
                item.setAmount(item.getAmount() - space);
                ItemStack cloned = item.clone();
                cloned.setAmount(maxAmount);
                grid[index] = cloned;
            } else {
                item.setAmount(gridItemAmount + itemAmount);
                grid[index] = item.clone();
                item.setAmount(0);
            }
        }
        data.setGrid(grid);
    }

    public ItemStack addSingleItem(Inventory inv, ItemStack ingredient, ItemStack gridItem) {
        for (ItemStack item : inv.getContents()) {
            if (item == null || item.getType().isAir() || !item.isSimilar(ingredient)) {
                continue;
            }
            if (gridItem == null || gridItem.getType().isAir()) {
                ItemStack cloned = item.clone();
                cloned.setAmount(1);
                inv.removeItem(cloned);
                return cloned;
            }else {
                if (isFull(gridItem)) {
                    return null;
                }
                ItemStack cloned = item.clone();
                cloned.setAmount(1);
                inv.removeItem(cloned);
                cloned.setAmount(gridItem.getAmount() + 1);
                return cloned;
            }
        }
        return null;
    }

    public void addItemsBySingle(Inventory inv, AutoCrafterData data) {
        if (inv.isEmpty()) {
            return;
        }
        ItemStack[] grid = data.getGrid();
        if (grid == null) {
            return;
        }
        boolean run = false;
        for (int i = 0; i < size; i++) {
            ItemStack ingredient = ingredients[i];
            if (ingredient == null || ingredient.getType().isAir()) {
                continue;
            }
            ItemStack gridItem = grid[i];
            ItemStack item = addSingleItem(inv, ingredient, gridItem);
            if (item != null) {
                grid[i] = item;
                run = true;
            }
        }
        data.setGrid(grid);
        if (run) {
            addItemsBySingle(inv, data);
        }
    }

    public void addItemsByStack(Inventory inv, AutoCrafterData data) {
        if (inv.isEmpty()) {
            return;
        }
        ItemStack[] grid = data.getGrid();
        if (grid == null) {
            return;
        }
        for (int i = 0; i < size; i++) {
            ItemStack ingredient = ingredients[i];
            if (ingredient == null || ingredient.getType().equals(Material.AIR)) {
                continue;
            }
            for (ItemStack item : inv.getContents()) {
                if (item == null || item.getType().equals(Material.AIR)|| !item.isSimilar(ingredient)) {
                    continue;
                }
                ItemStack gridItem = grid[i];
                if (gridItem == null || gridItem.getType().equals(Material.AIR)) {
                    inv.removeItem(item);
                    grid[i] = item;
                }else {
                    if (isFull(gridItem)) {
                        continue;
                    }
                    int itemAmount = item.getAmount();
                    int gridItemAmount = gridItem.getAmount();
                    int maxAmount = gridItem.getMaxStackSize();
                    int space = maxAmount - gridItemAmount;
                    if (itemAmount > space) {
                        ItemStack cloned = item.clone();
                        cloned.setAmount(item.getAmount() - space);
                        inv.removeItem(cloned);
                        cloned.setAmount(maxAmount);
                        grid[i] = cloned;
                    }else {
                        inv.removeItem(item);
                        ItemStack cloned = item.clone();
                        cloned.setAmount(gridItemAmount + itemAmount);
                        grid[i] = cloned;
                    }
                }
            }
        }
        data.setGrid(grid);
    }

    public void removeItems(Inventory inv, AutoCrafterData data) {
        if (inv.firstEmpty() == -1) {
            return;
        }
        Location location = inv.getLocation();
        if (location == null) {
            return;
        }
        ItemStack result = data.getResult();
        if (result == null) {
            return;
        }
        if (plugin.hasHopper()) {
            HopperData hopperData = new HopperData(location);
            if (hopperData.getMode().equals(HopperMode.WHITELIST)) {
                if (!hopperData.canGoThrough(result)) {
                    return;
                }
            } else {
                if (hopperData.canGoThrough(result)) {
                    return;
                }
            }
        }
        inv.addItem(result);
        data.setResult(null);
    }

    public boolean isFull(ItemStack item) {
        if (item == null) {
            return false;
        }
        return item.getAmount() >= item.getMaxStackSize();
    }

    public boolean isFull(ItemStack[] grid) {
        if (grid == null) {
            return false;
        }
        for (int i = 0; i < size; i++) {
            ItemStack ingredient = this.ingredients[i];
            if (ingredient == null || ingredient.getType().equals(Material.AIR)) {
                continue;
            }
            ItemStack gridItem = grid[i];
            if (gridItem == null || gridItem.getType().equals(Material.AIR) || !gridItem.isSimilar(ingredient) || !isFull(gridItem)) {
                return false;
            }
        }
        return true;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public ItemStack[] getIngredients() {
        return ingredients;
    }

    public ItemStack getResult() {
        return result;
    }

    public long getTime() {
        return time;
    }

    public long getPower() {
        return power;
    }

    public int getSize() {
        return size;
    }
}
