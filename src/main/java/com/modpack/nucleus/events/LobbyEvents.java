package com.modpack.nucleus.events;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public class LobbyEvents {

    public static final int LOBBY_RADIUS = 50;

    public static void register() {

        // ── Sin rotura de bloques en el lobby ─────────────────
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (!(player instanceof ServerPlayerEntity)) return true;
            if (inLobby(pos)) {
                player.sendMessage(Text.literal(
                    "§c✗ No puedes romper bloques en el lobby."), false);
                return false;
            }
            return true;
        });

        // ── Sin PvP en el lobby ───────────────────────────────
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (!(player instanceof ServerPlayerEntity)) return ActionResult.PASS;
            if (!(entity instanceof PlayerEntity)) return ActionResult.PASS;

            if (inLobby(entity.getBlockPos()) || inLobby(player.getBlockPos())) {
                player.sendMessage(Text.literal(
                    "§c✗ No hay PvP en el lobby."), false);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    public static boolean inLobby(BlockPos pos) {
        double dist = Math.sqrt(pos.getX() * pos.getX() + pos.getZ() * pos.getZ());
        return dist <= LOBBY_RADIUS;
    }
}
