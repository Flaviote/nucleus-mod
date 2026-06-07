package com.modpack.nucleus.block;

import com.modpack.nucleus.entity.NucleusBlockEntity;
import com.modpack.nucleus.init.NucleusBlockEntities;
import com.modpack.nucleus.state.NucleusStateManager;
import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NucleusBlock extends BlockWithEntity implements PolymerBlock {

    public static final MapCodec<NucleusBlock> CODEC = createCodec(NucleusBlock::new);

    public NucleusBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state) {
        return Blocks.BEACON.getDefaultState();
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NucleusBlockEntity(pos, state);
    }

    public BlockEntityType<? extends NucleusBlockEntity> getPolymerBlockEntityType() {
        return NucleusBlockEntities.NUCLEUS_BLOCK_ENTITY;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                         @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world.isClient) return;
        if (!(placer instanceof ServerPlayerEntity player)) return;

        double dist = Math.sqrt(pos.getX() * pos.getX() + pos.getZ() * pos.getZ());
        if (dist <= 50) {
            world.breakBlock(pos, false);
            player.getInventory().insertStack(new ItemStack(this));
            player.sendMessage(Text.literal(
                "§c✗ No puedes colocar tu Núcleo dentro del lobby."), false);
