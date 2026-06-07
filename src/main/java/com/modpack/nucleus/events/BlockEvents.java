package com.modpack.nucleus.events;

import com.modpack.nucleus.entity.NucleusBlockEntity;
import com.modpack.nucleus.state.NucleusStateManager;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.UUID;

public class BlockEvents {

    private static final String NUCLEUS_BLOCK_ID = "nucleus:nucleus_block";

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {

            if (!(player instanceof ServerPlayerEntity serverPlayer)) return true;
            if (!(world instanceof ServerWorld serverWorld)) return true;

            UUID uuid = serverPlayer.getUuid();
            NucleusStateManager stateManager =
                NucleusStateManager.get(serverWorld.getServer());

            String blockId = Registries.BLOCK.getId(state.getBlock()).toString();

            // ── Bloque Núcleo ─────────────────────────────────
            if (blockId.equals(NUCLEUS_BLOCK_ID)) {

                boolean hasNetheritePickaxe =
                    serverPlayer.getMainHandStack().getItem() == Items.NETHERITE_PICKAXE;

                if (!hasNetheritePickaxe) {
                    serverPlayer.sendMessage(Text.literal(
                        "§c✗ El Núcleo solo puede destruirse con un §fpico de netherita§c."
                    ), false);
                    return false;
                }

                if (!(blockEntity instanceof NucleusBlockEntity nucleusBE)) return false;

                String ownerUUIDStr = nucleusBE.getOwnerUUID();
                String ownerName   = nucleusBE.getOwnerName();

                if (ownerUUIDStr == null || ownerUUIDStr.isEmpty()) return true;

                UUID ownerUUID = UUID.fromString(ownerUUIDStr);

                // El propietario no puede romper su propio Núcleo
                if (ownerUUID.equals(uuid)) {
                    serverPlayer.sendMessage(Text.literal(
                        "§c✗ No puedes destruir tu propio Núcleo."
                    ), false);
                    return false;
                }

                // Anunciar eliminación
                serverWorld.getServer().getPlayerManager().broadcast(
                    Text.literal("§4☠ §fEl Núcleo de §c" + ownerName +
                        "§f fue destruido por §e" +
                        serverPlayer.getName().getString() +
                        "§f. ¡Ha sido eliminado!"),
                    false
                );

                // Marcar como eliminado
                stateManager.eliminate(ownerUUID);

                // Expulsar al propietario si está online
                ServerPlayerEntity owner = serverWorld.getServer()
                    .getPlayerManager().getPlayer(ownerUUID);
                if (owner != null) {
                    owner.networkHandler.disconnect(
                        Text.literal("§4Tu Núcleo fue destruido. Partida terminada.")
                    );
                }

                return true;
            }

            // ── Fase 0: solo madera ───────────────────────────
            if (stateManager.getPhase(uuid) != 0) return true;

            boolean isWood = state.isIn(BlockTags.LOGS)
                || state.isIn(BlockTags.LEAVES);

            if (!isWood) {
                serverPlayer.sendMessage(Text.literal(
                    "§c✗ Solo puedes talar madera hasta colocar tu Núcleo."
                ), false);
                return false;
            }

            return true;
        });
    }
}
