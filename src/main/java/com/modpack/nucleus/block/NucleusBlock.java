package com.modpack.nucleus.block;

import com.modpack.nucleus.entity.NucleusBlockEntity;
import com.modpack.nucleus.init.NucleusBlockEntities;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NucleusBlock extends BlockWithEntity implements PolymerBlock {

    public NucleusBlock(Settings settings) {
        super(settings);
    }

    // ── Polymer: qué bloque vanilla ve el cliente ─────────────
    // Beacon como base — Polymer aplica nuestra textura encima
    @Override
    public Block getPolymerBlock(BlockState state) {
        return Blocks.BEACON;
    }

    // ── Block Entity ──────────────────────────────────────────
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NucleusBlockEntity(pos, state);
    }

    @Override
    public BlockEntityType<NucleusBlockEntity> getPolymerBlockEntity() {
        return NucleusBlockEntities.NUCLEUS_BLOCK_ENTITY;
    }

    // ── Al romper: limpiar NBT del propietario ────────────────
    // La lógica de eliminación la gestiona KubeJS (nucleus_core.js)
    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && world.getBlockEntity(pos) instanceof NucleusBlockEntity be) {
            be.clearOwner();
        }
        super.onBreak(world, pos, state, player);
    }
}
