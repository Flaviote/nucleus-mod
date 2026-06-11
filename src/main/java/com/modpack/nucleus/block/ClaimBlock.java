package com.modpack.nucleus.block;

import com.modpack.nucleus.state.NucleusStateManager;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ClaimBlock extends Block {

    public static final MapCodec<ClaimBlock> CODEC = createCodec(ClaimBlock::new);

    public ClaimBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state,
                         @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        if (world.isClient) return;
        if (!(placer instanceof ServerPlayerEntity player)) return;

        NucleusStateManager sm = NucleusStateManager.get(player.getServer());

        if (sm.hasClaimZone(player.getUuid())) {
            player.sendMessage(Text.literal(
                "§e⚠ Tu zona de protección anterior ha sido eliminada."), false);
        }

        sm.setClaimZone(player.getUuid(), pos.getX(), pos.getY(), pos.getZ());

        player.sendMessage(Text.literal(
            "§a✔ Zona de protección establecida (30×30 bloques centrada aquí)."), false);
        player.sendMessage(Text.literal(
            "§7Solo tú puedes romper este bloque para moverlo."), false);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
            NucleusStateManager sm = NucleusStateManager.get(serverPlayer.getServer());
            int[] zone = sm.getClaimZone(serverPlayer.getUuid());

            if (zone != null && zone[0] == pos.getX() && zone[2] == pos.getZ()) {
                sm.removeClaimZone(serverPlayer.getUuid());
                if (!player.isCreative()) {
                    player.getInventory().insertStack(
                        new ItemStack(NucleusBlocks.CLAIM_BLOCK_ITEM)
                    );
                }
                player.sendMessage(Text.literal(
                    "§7Zona de protección eliminada. Puedes colocar el bloque en otro lugar."), false);
            } else {
                player.sendMessage(Text.literal(
                    "§c✗ No puedes romper la zona de protección de otro jugador."), false);
                return state;
            }
        }
        return super.onBreak(world, pos, state, player);
    }
}
