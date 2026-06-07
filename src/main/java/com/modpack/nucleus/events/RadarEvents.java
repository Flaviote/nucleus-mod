package com.modpack.nucleus.events;

import com.modpack.nucleus.state.NucleusStateManager;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;

import java.util.concurrent.atomic.AtomicInteger;

public class RadarEvents {

    private static final int RADAR_RANGE = 1000;
    private static final int DIST_STEP   = 50;

    public static void register() {

        UseItemCallback.EVENT.register((player, world, hand) -> {

            if (world.isClient()) return TypedActionResult.pass(player.getStackInHand(hand));
            if (!(player instanceof ServerPlayerEntity serverPlayer)) {
                return TypedActionResult.pass(player.getStackInHand(hand));
            }

            ItemStack held = player.getStackInHand(hand);
            String itemId  = Registries.ITEM.getId(held.getItem()).toString();

            if (!itemId.equals("nucleus:radar")) {
                return TypedActionResult.pass(held);
            }

            NucleusStateManager state = NucleusStateManager.get(serverPlayer.getServer());
            double px = serverPlayer.getX();
            double pz = serverPlayer.getZ();
            AtomicInteger found = new AtomicInteger(0);

            serverPlayer.getServer().getPlayerManager().getPlayerList().forEach(other -> {
                if (other.getUuid().equals(serverPlayer.getUuid())) return;
                if (state.getPhase(other.getUuid()) != 1) return;

                int[] pos = state.getNucleusPosition(other.getUuid());
                if (pos == null) return;

                double dx   = pos[0] - px;
                double dz   = pos[2] - pz;
                double dist = Math.sqrt(dx * dx + dz * dz);

                if (dist > RADAR_RANGE) return;

                int approx = (int)(Math.round(dist / DIST_STEP) * DIST_STEP);
                String dir = getCardinalDirection(Math.toDegrees(Math.atan2(dz, dx)));

                serverPlayer.sendMessage(Text.literal(
                    "§b[RADAR] §fNúcleo detectado — §e~" + approx +
                    " bloques §fal §a" + dir), false);

                found.incrementAndGet();
            });

            if (found.get() == 0) {
                serverPlayer.sendMessage(Text.literal(
                    "§b[RADAR] §7No se detectaron núcleos en el rango."), false);
            } else {
                serverPlayer.sendMessage(Text.literal(
                    "§b[RADAR] §7" + found.get() +
                    " núcleo(s) detectado(s). El radar se ha agotado."), false);
            }

            held.decrement(1);
            return TypedActionResult.success(held);
        });
    }

    private static String getCardinalDirection(double angle) {
        if (angle < -157.5 || angle >= 157.5) return "OESTE ←";
        if (angle < -112.5)                   return "NOROESTE ↖";
        if (angle < -67.5)                    return "NORTE ↑";
        if (angle < -22.5)                    return "NORESTE ↗";
        if (angle < 22.5)                     return "ESTE →";
        if (angle < 67.5)                     return "SURESTE ↘";
        if (angle < 112.5)                    return "SUR ↓";
        return "SUROESTE ↙";
    }
}
