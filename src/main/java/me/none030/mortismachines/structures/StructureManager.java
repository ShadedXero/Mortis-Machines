package me.none030.mortismachines.structures;

import org.bukkit.Location;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class StructureManager {

    private final Vector radius;
    private final List<Structure> structures;
    private final HashMap<String, Structure> structureById;

    public StructureManager(Vector radius) {
        this.radius = radius;
        this.structures = new ArrayList<>();
        this.structureById = new HashMap<>();
    }

    public Structure getStructure(Location center, String id, boolean strict) {
        StructureBlock core = new StructureBlock(center.getBlock().getType(), center.getBlock().getBlockData(), new Vector(0, 0, 0), strict);
        List<StructureBlock> structureBlocks = new ArrayList<>();
        for (double x = center.getX() - radius.getX(); x <= center.getX() + radius.getX(); x++) {
            for (double y = center.getY() - radius.getY(); y <= center.getY() + radius.getY(); y++) {
                for (double z = center.getZ() - radius.getZ(); z <= center.getZ() + radius.getZ(); z++) {
                    Location loc = new Location(center.getWorld(), x, y, z);
                    Vector vector = new Vector(loc.getX() - center.getX(), loc.getY() - center.getY(), loc.getZ() - center.getZ());
                    if (vector.getX() == 0 && vector.getY() == 0 && vector.getZ() == 0) {
                        continue;
                    }
                    Material block = loc.getBlock().getType();
                    if (block.equals(Material.AIR)) {
                        continue;
                    }
                    if (block.equals(Material.BARRIER)) {
                        block = Material.AIR;
                    }
                    StructureBlock structureBlock = new StructureBlock(block, loc.getBlock().getBlockData(), vector, strict);
                    structureBlocks.add(structureBlock);
                }
            }
        }
        return new Structure(id, core, structureBlocks);
    }

    public void saveStructure(Structure structure) {
        structures.add(structure);
        structureById.put(structure.getId(), structure);
    }

    public void deleteStructure(String id) {
        Structure structure = structureById.get(id);
        if (structure == null) {
            return;
        }
        structures.remove(structure);
        structureById.remove(id);
    }

    public Vector getRadius() {
        return radius;
    }

    public List<Structure> getStructures() {
        return structures;
    }

    public HashMap<String, Structure> getStructureById() {
        return structureById;
    }
}
