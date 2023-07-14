package com.mortisdevelopment.mortismachines.machines.idcard;

import com.mortisdevelopment.mortismachines.data.MachineData;
import com.mortisdevelopment.mortismachines.utils.MachineType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class IdCardData extends MachineData {

    private final String timeKey = "MortisMachinesTime";
    private final String editorsKey = "MortisMachinesEditors";
    private final String keyCardKey = "MortisMachinesKeyCard";
    private final String insertKeyCardKey = "MortisMachinesInsertKeyCard";
    private final String pulseKey = "MortisMachinesPulse";

    public IdCardData(Location core) {
        super(core, MachineType.ID_CARD);
    }

    public void create(String id, String structureId, boolean pulse, long time, String uuid) {
        create(id, structureId);
        setPulse(pulse);
        setTime(time);
        set(editorsKey, uuid);
    }

    public boolean isPulse() {
        String value = get(pulseKey);
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(value);
    }

    public void setPulse(boolean pulse) {
        set(pulseKey, Boolean.toString(pulse));
    }

    public long getTime() {
        String value = get(timeKey);
        if (value == null) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public void setTime(long time) {
        set(timeKey, Long.toString(time));
    }

    public void setKeyCard(ItemStack item) {
        item = item.clone();
        item.setAmount(1);
        set(keyCardKey, serialize(item));
    }

    public ItemStack getKeyCard() {
        String raw = get(keyCardKey);
        if (raw == null) {
            return null;
        }
        return deserialize(raw);
    }

    public ItemStack getInsertKeyCard() {
        String raw = get(insertKeyCardKey);
        if (raw == null) {
            return null;
        }
        return deserialize(raw);
    }

    public void empty(Location location) {
        ItemStack keyCard = getInsertKeyCard();
        if (keyCard == null) {
            return;
        }
        drop(location, keyCard);
    }

    private void drop(Location location, ItemStack item) {
        location.getWorld().dropItemNaturally(location, item);
    }

    public void setInsertKeyCard() {
        set(insertKeyCardKey, get(keyCardKey));
    }

    public void removeInsertKeyCard() {
        set(insertKeyCardKey, null);
    }

    public boolean isKeyCard(ItemStack item) {
        String raw = get(keyCardKey);
        if (raw == null) {
            return false;
        }
        ItemStack keyCard = deserialize(raw);
        if (keyCard == null) {
            return false;
        }
        return keyCard.isSimilar(item);
    }

    public String getEditors() {
        String rawEditors = get(editorsKey);
        if (rawEditors == null) {
            return null;
        }
        List<UUID> uuids = new ArrayList<>(rawStringToUUIDList(rawEditors));
        List<String> names = new ArrayList<>(UUIDListToNamesList(uuids));
        return StringListToRaw(names);
    }

    public void addEditor(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString();
        String rawEditors = get(editorsKey);
        if (rawEditors == null) {
            set(editorsKey, uuid);
            return;
        }
        List<UUID> uuids = new ArrayList<>(rawStringToUUIDList(rawEditors));
        if (uuids.contains(player.getUniqueId())) {
            return;
        }
        set(editorsKey, rawEditors + "," + uuid);
    }

    public void removeEditor(OfflinePlayer player) {
        String rawEditors = get(editorsKey);
        if (rawEditors == null) {
            return;
        }
        List<UUID> uuids = new ArrayList<>(rawStringToUUIDList(rawEditors));
        uuids.removeIf(uuid -> player.getUniqueId().equals(uuid));
        String raw = UUIDListToRaw(uuids);
        set(editorsKey, raw);
    }

    public boolean isEditor(OfflinePlayer player) {
        String rawEditors = get(editorsKey);
        if (rawEditors == null) {
            return false;
        }
        List<UUID> uuids = new ArrayList<>(rawStringToUUIDList(rawEditors));
        return uuids.contains(player.getUniqueId());
    }

    public boolean isEditorLimit() {
        String raw = get(editorsKey);
        if (raw == null) {
            return false;
        }
        String[] rawEditors = raw.split(",");
        return rawEditors.length >= 10;
    }

    private List<String> UUIDListToNamesList(List<UUID> list) {
        List<String> names = new ArrayList<>();
        for (UUID uuid : list) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            names.add(player.getName());
        }
        return names;
    }

    private List<UUID> rawStringToUUIDList(String raw) {
        List<UUID> uuids = new ArrayList<>();
        for (String line : raw.split(",")) {
            uuids.add(UUID.fromString(line));
        }
        return uuids;
    }

    private String StringListToRaw(List<String> list) {
        StringBuilder raw = new StringBuilder();
        for (String line : list) {
            raw.append(",").append(line);
        }
        return raw.toString();
    }

    private String UUIDListToRaw(List<UUID> list) {
        StringBuilder raw = new StringBuilder();
        for (UUID uuid : list) {
            raw.append(",").append(uuid.toString());
        }
        return raw.toString();
    }
}
