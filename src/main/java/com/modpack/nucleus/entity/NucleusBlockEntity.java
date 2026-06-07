package com.modpack.nucleus.entity;

import com.modpack.nucleus.init.NucleusBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class NucleusBlockEntity extends BlockEntity {

    // Datos del propietario — leídos por KubeJS con getPersistentData()
    private String ownerUUID = "";
    private String ownerName = "";

    public NucleusBlockEntity(BlockPos pos, BlockState state) {
        super(NucleusBlockEntities.NUCLEUS_BLOCK_ENTITY, pos, state);
    }

    // ── Setters ───────────────────────────────────────────────
    public void setOwner(String uuid, String name) {
        this.ownerUUID = uuid;
        this.ownerName = name;
        markDirty();
    }

    public void clearOwner() {
        this.ownerUUID = "";
        this.ownerName = "";
        markDirty();
    }

    // ── Getters ───────────────────────────────────────────────
    public String getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public boolean hasOwner()    { return !ownerUUID.isEmpty(); }

    // ── NBT: persistencia entre reinicios ─────────────────────
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("NucleusOwner",     ownerUUID);
        nbt.putString("NucleusOwnerName", ownerName);
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.ownerUUID = nbt.getString("NucleusOwner");
        this.ownerName = nbt.getString("NucleusOwnerName");
    }

    // ── Sincronización con cliente (necesario para Polymer) ───
    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return createNbt(registries);
    }
}
