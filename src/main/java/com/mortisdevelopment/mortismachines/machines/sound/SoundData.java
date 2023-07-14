package com.mortisdevelopment.mortismachines.machines.sound;

import com.mortisdevelopment.mortismachines.utils.MachineType;
import com.mortisdevelopment.mortismachines.data.MachineData;
import org.bukkit.Location;

public class SoundData extends MachineData {

    private final String idKey = "MortisMachinesId";
    private final String structureIdKey = "MortisMachinesStructureId";
    private final String messageKey = "MortisMachinesMessage";

    public SoundData(Location core) {
        super(core, MachineType.SOUND);
    }

    public String getId() {
        return get(idKey);
    }

    public String getStructureId() {
        return get(structureIdKey);
    }

    public String getMessage() {
        return get(messageKey);
    }

    public void setMessage(String message) {
        set(messageKey, message);
    }
}
