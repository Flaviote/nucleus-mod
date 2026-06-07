package com.modpack.nucleus.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class NucleusStateManager extends PersistentState {

    private static final String SAVE_KEY = "nucleus_state";

    private final Map<UUID, Integer> phases           = new HashMap<>();
    private final Map<UUID, int[]>   nucleusPositions = new HashMap<>();
    private final Set<UUID>          eliminated       = new HashSet<>();
    private final Set<UUID>          initialized      = new HashSet<>();

    public int getPhase(UUID player) {
        return phases.getOrDefault(player, 0);
    }

    public void setPhase(UUID player, int phase) {
        phases.put(player, phase);
        markDirty();
    }

    public boolean isInitialized(UUID player) {
        return initialized.contains(player);
    }

    public void setInitialized(UUID player) {
        initialized.add(player);
        markDirty();
    }

    public void setNucleusPosition(UUID player, int x, int y, int z) {
        nucleusPositions.put(player, new int[]{x, y, z});
        markDirty();
    }

    public int[] getNucleusPosition(UUID player) {
        return nucleusPositions.get(player);
    }

    public boolean hasNucleus(UUID player) {
        return nucleusPositions.containsKey(player);
    }

    public void eliminate(UUID player) {
        eliminated.add(player);
        phases.remove(player);
        nucleusPositions.remove(player);
        markDirty();
    }

    public boolean isEliminated(UUID player) {
        return eliminated.contains(player);
    }

    public void resetAll() {
        phases.clear();
        nucleusPositions.clear();
        eliminated.clear();
        initialized.clear();
        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound phasesNbt = new NbtCompound();
        phases.forEach((uuid, phase) -> phasesNbt.putInt(uuid.toString(), phase));
        nbt.put("phases", phasesNbt);

        NbtCompound posNbt = new NbtCompound();
        nucleusPositions.forEach((uuid, pos) -> {
            NbtCompound posEntry = new NbtCompound();
            posEntry.putInt("x", pos[0]);
            posEntry.putInt("y", pos[1]);
            posEntry.putInt("z", pos[2]);
            posNbt.put(uuid.toString(), posEntry);
        });
        nbt.put("positions", posNbt);

        NbtList eliminatedList = new NbtList();
        eliminated.forEach(uuid -> eliminatedList.add(NbtString.of(uuid.toString())));
        nbt.put("eliminated", eliminatedList);

        NbtList initializedList = new NbtList();
        initialized.forEach(uuid -> initializedList.add(NbtString.of(uuid.toString())));
        nbt.put("initialized", initializedList);

        return nbt;
    }

    public static NucleusStateManager fromNbt(NbtCompound nbt) {
        NucleusStateManager state = new NucleusStateManager();

        NbtCompound phasesNbt = nbt.getCompound("phases");
        phasesNbt.getKeys().forEach(key ->
            state.phases.put(UUID.fromString(key), phasesNbt.getInt(key)));

        NbtCompound posNbt = nbt.getCompound("positions");
        posNbt.getKeys().forEach(key -> {
            NbtCompound posEntry = posNbt.getCompound(key);
            state.nucleusPositions.put(UUID.fromString(key), new int[]{
                posEntry.getInt("x"),
                posEntry.getInt("y"),
                posEntry.getInt("z")
            });
        });

        NbtList eliminatedList = nbt.getList("eliminated", 8);
        eliminatedList.forEach(tag -> state.eliminated.add(UUID.fromString(tag.asString())));

        NbtList initializedList = nbt.getList("initialized", 8);
        initializedList.forEach(tag -> state.initialized.add(UUID.fromString(tag.asString())));

        return state;
    }

    public static NucleusStateManager get(MinecraftServer server) {
        PersistentStateManager manager = server
            .getWorld(World.OVERWORLD)
            .getPersistentStateManager();

        return manager.getOrCreate(
            NucleusStateManager::fromNbt,
            NucleusStateManager::new,
            SAVE_KEY
        );
    }
}
