package com.modpack.nucleus.entity;

import com.modpack.nucleus.init.NucleusBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

public class NucleusBlockEntity extends BlockEntity {

    private String ownerUUID = "";
    private String ownerName = "";
    private Text customName  = null;

    public NucleusBlockEntity(BlockPos pos, BlockState state) {
        super(NucleusBlockEntities.NUCLEUS_BLOCK_ENTITY, pos, state);
    }

    public void setOwner(String uuid, String name) {
        this.ownerUUID = uuid;
        this.ownerName = name;
        markDirty();
    }

    public void setCustomName(Text name) {
        this.customName = name;
        markDirty();
    }

    public void clearOwner() {
        this.ownerUUID  = "";
        this.ownerName  = "";
        this.customName = null;
        markDirty();
    }

    public String getOwnerUUID() { return ownerUUID; }
    public String getOwnerName() { return ownerName; }
    public boolean hasOwner()    { return !ownerUUID.isEmpty(); }

    @Nullable
    public Text getCustomName() { return customName; }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.writeNbt(nbt, registries);
        nbt.putString("NucleusOwner",     ownerUUID);
        nbt.putString("NucleusOwnerName", ownerName);
        if (customName != null) {
            nbt.putString("CustomName",
                Text.Serialization.toJsonString(customName, registries));
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registries) {
        super.readNbt(nbt, registries);
        this.ownerUUID = nbt.getString("NucleusOwner");
        this.ownerName = nbt.getString("NucleusOwnerName");
        if (nbt.contains("CustomName")) {
            this.customName = Text.Serialization.fromJson(
                nbt.getString("CustomName"), registries);
        }
    }

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
