package com.modpack.nucleus.events;

import com.modpack.nucleus.init.NucleusBlocks;
import com.modpack.nucleus.state.NucleusStateManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.UUID;

public class PlayerEvents {

    public static final int LOBBY_X = 0;
    public static final int LOBBY_Y = 100;
    public static final int LOBBY_Z = 0;

    public static void register() {

        // ── AL CONECTARSE ─────────────────────────────────────
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();
            NucleusStateManager state = NucleusStateManager.get(server);

            // Expulsar si fue eliminado
            if (state.isEliminated(uuid)) {
                server.execute(() ->
                    player.networkHandler.disconnect(
                        Text.literal("§4Tu Núcleo fue destruido. Tu partida ha terminado.")
                    )
                );
                return;
            }

            // Primera conexión
            if (!state.isInitialized(uuid)) {
                state.setInitialized(uuid);
                state.setPhase(uuid, 0);

                server.execute(() -> {
                    // Dar el ítem Núcleo
                    ItemStack nucleusItem = new ItemStack(NucleusBlocks.NUCLEUS_BLOCK_ITEM);
                    player.getInventory().insertStack(nucleusItem);

                    // Teletransportar al lobby
                    player.teleport(
                        server.getWorld(net.minecraft.world.World.OVERWORLD),
                        LOBBY_X, LOBBY_Y, LOBBY_Z, 0f, 0f
                    );

                    player.sendMessage(Text.literal(
                        "§a¡Bienvenido! §7Has recibido tu §dNúcleo Personal§7."), false);
                    player.sendMessage(Text.literal(
                        "§7Colócalo en el mundo para comenzar a jugar."), false);
                    player.sendMessage(Text.literal(
                        "§c⚠ Hasta colocarlo, §fsolo podrás talar madera§c."), false);
                });
            }
        });

        // ── AL RESPAWNEAR ─────────────────────────────────────
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            UUID uuid = newPlayer.getUuid();
            NucleusStateManager state = NucleusStateManager.get(newPlayer.getServer());

            if (state.getPhase(uuid) == 0) {
                newPlayer.teleport(
                    newPlayer.getServer().getWorld(net.minecraft.world.World.OVERWORLD),
                    LOBBY_X, LOBBY_Y, LOBBY_Z, 0f, 0f
                );
            }
        });
    }
}
