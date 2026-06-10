package com.modpack.nucleus.state;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.RegistryWrapper;
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

    private static final String SAVE_KEY    = "nucleus_state";
    public  static final int    ZONE_RADIUS = 15;

    private final Map<UUID, Integer> phases           = new HashMap<>();
    private final Map<UUID, int[]>   nucleusPositions = new HashMap<>();
    private final Set<UUID>          eliminated       = new HashSet<>();
    private final Set<UUID>          initialized      = new HashSet<>();
    private final Map<UUID, int[]>   claimZones       = new HashMap<>();

    public int getPhase(UUID p)             { return phases.getOrDefault(p, 0); }
    public void setPhase(UUID p, int phase) { phases.put(p, phase); markDirty(); }
    public boolean isInitialized(UUID p)    { return initialized.contains(p); }
    public void setInitialized(UUID p)      { initialized.add(p); markDirty(); }
    public void setNucleusPosition(UUID p, int x, int y, int z) { nucleusPositions.put(p, new int[]{x,y,z}); markDirty(); }
    public int[] getNucleusPosition(UUID p) { return nucleusPositions.get(p); }
    public boolean hasNucleus(UUID p)       { return nucleusPositions.containsKey(p); }

    public void setClaimZone(UUID p, int cx, int cy, int cz) { claimZones.put(p, new int[]{cx,cy,cz}); markDirty(); }
    public void removeClaimZone(UUID p)     { claimZones.remove(p); markDirty(); }
    public boolean hasClaimZone(UUID p)     { return claimZones.containsKey(p); }
    public int[] getClaimZone(UUID p)       { return claimZones.get(p); }
    public Map<UUID, int[]> getAllClaimZones() { return claimZones; }

    public UUID getZoneOwnerAt(int x, int z) {
        for (Map.Entry<UUID, int[]> e : claimZones.entrySet()) {
            int[] c = e.getValue();
            if (Math.abs(x - c[0]) <= ZONE_RADIUS && Math.abs(z - c[2]) <= ZONE_RADIUS)
                return e.getKey();
        }
        return null;
    }

    public boolean isInZoneOf(UUID owner, int x, int z) {
        int[] c = claimZones.get(owner);
        return c != null && Math.abs(x - c[0]) <= ZONE_RADIUS && Math.abs(z - c[2]) <= ZONE_RADIUS;
    }

    public void eliminate(UUID p) {
        eliminated.add(p); phases.remove(p); nucleusPositions.remove(p); claimZones.remove(p); markDirty();
    }
    public boolean isEliminated(UUID p) { return eliminated.contains(p); }

    public void resetAll() {
        phases.clear(); nucleusPositions.clear(); eliminated.clear(); initialized.clear(); claimZones.clear(); markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup reg) {
        NbtCompound ph = new NbtCompound();
        phases.forEach((u,v) -> ph.putInt(u.toString(), v));
        nbt.put("phases", ph);

        NbtCompound pos = new NbtCompound();
        nucleusPositions.forEach((u,v) -> { NbtCompound e = new NbtCompound(); e.putInt("x",v[0]); e.putInt("y",v[1]); e.putInt("z",v[2]); pos.put(u.toString(),e); });
        nbt.put("positions", pos);

        NbtCompound zones = new NbtCompound();
        claimZones.forEach((u,v) -> { NbtCompound e = new NbtCompound(); e.putInt("x",v[0]); e.putInt("y",v[1]); e.putInt("z",v[2]); zones.put(u.toString(),e); });
        nbt.put("zones", zones);

        NbtList el = new NbtList(); eliminated.forEach(u -> el.add(NbtString.of(u.toString()))); nbt.put("eliminated", el);
        NbtList in = new NbtList(); initialized.forEach(u -> in.add(NbtString.of(u.toString()))); nbt.put("initialized", in);
        return nbt;
    }

    public static NucleusStateManager fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup reg) {
        NucleusStateManager s = new NucleusStateManager();
        NbtCompound ph = nbt.getCompound("phases"); ph.getKeys().forEach(k -> s.phases.put(UUID.fromString(k), ph.getInt(k)));
        NbtCompound pos = nbt.getCompound("positions"); pos.getKeys().forEach(k -> { NbtCompound e = pos.getCompound(k); s.nucleusPositions.put(UUID.fromString(k), new int[]{e.getInt("x"),e.getInt("y"),e.getInt("z")}); });
        NbtCompound zones = nbt.getCompound("zones"); zones.getKeys().forEach(k -> { NbtCompound e = zones.getCompound(k); s.claimZones.put(UUID.fromString(k), new int[]{e.getInt("x"),e.getInt("y"),e.getInt("z")}); });
        nbt.getList("eliminated",8).forEach(t -> s.eliminated.add(UUID.fromString(t.asString())));
        nbt.getList("initialized",8).forEach(t -> s.initialized.add(UUID.fromString(t.asString())));
        return s;
    }

    public static NucleusStateManager get(MinecraftServer server) {
        return server.getWorld(World.OVERWORLD).getPersistentStateManager()
            .getOrCreate(new PersistentState.Type<>(NucleusStateManager::new, NucleusStateManager::fromNbt, null), SAVE_KEY);
    }
}
