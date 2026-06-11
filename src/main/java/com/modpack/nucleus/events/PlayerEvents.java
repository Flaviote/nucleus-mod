package com.modpack.nucleus.events;

import com.modpack.nucleus.init.NucleusBlocks;
import com.modpack.nucleus.state.NucleusStateManager;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerEvents {

    public static final int LOBBY_X = 0;
    public static final int LOBBY_Y = 100;
    public static final int LOBBY_Z = 0;

    private static final Map<UUID, long[]> lastPositions = new HashMap<>();
    private static int tickCounter = 0;

    public static void register() {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.getPlayer();
            UUID uuid = player.getUuid();
            NucleusStateManager state = NucleusStateManager.get(server);

            if (state.isEliminated(uuid)) {
                server.execute(() -> player.networkHandler.disconnect(
                    Text.literal("§4Tu Núcleo fue destruido. Tu partida ha terminado.")));
                return;
            }

            if (!state.isInitialized(uuid)) {
                state.setInitialized(uuid);
                state.setPhase(uuid, 0);

                server.execute(() -> {
                    player.getInventory().insertStack(new ItemStack(NucleusBlocks.NUCLEUS_BLOCK_ITEM));
                    player.getInventory().insertStack(new ItemStack(NucleusBlocks.CLAIM_BLOCK_ITEM));

                    player.teleport(server.getWorld(net.minecraft.world.World.OVERWORLD),
                        LOBBY_X, LOBBY_Y, LOBBY_Z, 0f, 0f);

                    player.sendMessage(Text.literal(
                        "§a¡Bienvenido! §7Has recibido tu §dNúcleo Personal §7y tu §bBloque de Protección§7."), false);
                    player.sendMessage(Text.literal(
                        "§7Coloca el §dNúcleo §7para comenzar. Coloca el §bBloque de Protección §7para asegurar tu base."), false);
                    player.sendMessage(Text.literal(
                        "§c⚠ Hasta colocar el Núcleo, no podrás picar §fhierro, oro, diamante ni netherita§c."), false);
                });
            }
        });

        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            UUID uuid = newPlayer.getUuid();
            NucleusStateManager state = NucleusStateManager.get(newPlayer.getServer());
            if (state.getPhase(uuid) == 0) {
                newPlayer.teleport(newPlayer.getServer().getWorld(net.minecraft.world.World.OVERWORLD),
                    LOBBY_X, LOBBY_Y, LOBBY_Z, 0f, 0f);
            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter < 20) return;
            tickCounter = 0;

            NucleusStateManager sm = NucleusStateManager.get(server);
            List<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList();

            for (ServerPlayerEntity player : players) {
                UUID uuid = player.getUuid();
                int cx = (int) player.getX();
                int cz = (int) player.getZ();

                long[] last = lastPositions.get(uuid);
                int lastX = last != null ? (int) last[0] : cx;
                int lastZ = last != null ? (int) last[1] : cz;

                lastPositions.put(uuid, new long[]{cx, cz});

                UUID currentOwner = sm.getZoneOwnerAt(cx, cz);
                UUID previousOwner = sm.getZoneOwnerAt(lastX, lastZ);

                if (currentOwner != null && !currentOwner.equals(uuid) &&
                    (previousOwner == null || !previousOwner.equals(currentOwner))) {

                    String ownerName = getPlayerName(server, currentOwner);

                    player.sendMessage(Text.literal(
                        "§e⚠ Estás entrando en la propiedad de §c" + ownerName + "§e."), false);

                    ServerPlayerEntity owner = server.getPlayerManager().getPlayer(currentOwner);
                    if (owner != null) {
                        owner.sendMessage(Text.literal(
                            "§c⚠ §f" + player.getName().getString() +
                            "§c está entrando en tu propiedad."), false);
                    }
                }

                if (previousOwner != null && !previousOwner.equals(uuid) &&
                    (currentOwner == null || !currentOwner.equals(previousOwner))) {

                    String ownerName = getPlayerName(server, previousOwner);
                    player.sendMessage(Text.literal(
                        "§7Has salido de la propiedad de §f" + ownerName + "§7."), false);
                }
            }
        });
    }

    private static String getPlayerName(MinecraftServer server, UUID uuid) {
        ServerPlayerEntity p = server.getPlayerManager().getPlayer(uuid);
        if (p != null) return p.getName().getString();
        var profile = server.getUserCache().getByUuid(uuid);
        return profile.map(com.mojang.authlib.GameProfile::getName).orElse("Desconocido");
    }
}
