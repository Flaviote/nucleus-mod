package com.modpack.nucleus.events;

import com.modpack.nucleus.entity.NucleusBlockEntity;
import com.modpack.nucleus.state.NucleusStateManager;
import net.minecraft.block.Blocks;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.UUID;

public class BlockEvents {

    private static final String NUCLEUS_BLOCK_ID = "nucleus:nucleus_block";
    private static final String CLAIM_BLOCK_ID   = "nucleus:claim_block";
    private static final String WARHAMMER_ID     = "nucleus:warhammer";

    private static boolean isRestrictedOre(net.minecraft.block.BlockState state) {
        net.minecraft.block.Block b = state.getBlock();
        return state.isIn(BlockTags.IRON_ORES)
            || state.isIn(BlockTags.GOLD_ORES)
            || state.isIn(BlockTags.DIAMOND_ORES)
            || b == Blocks.ANCIENT_DEBRIS
            || b == Blocks.NETHERITE_BLOCK;
    }

    public static void register() {

        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {

            if (!(player instanceof ServerPlayerEntity sp)) return true;
            if (!(world instanceof ServerWorld sw)) return true;

            UUID uuid = sp.getUuid();
            NucleusStateManager sm = NucleusStateManager.get(sw.getServer());
            String blockId = Registries.BLOCK.getId(state.getBlock()).toString();

            if (blockId.equals(NUCLEUS_BLOCK_ID)) {

                if (sp.getMainHandStack().getItem() != Items.NETHERITE_PICKAXE) {
                    sp.sendMessage(Text.literal(
                        "§c✗ El Núcleo solo puede destruirse con un §fpico de netherita§c."), false);
                    return false;
                }

                if (!(blockEntity instanceof NucleusBlockEntity be)) return false;
                String ownerUUIDStr = be.getOwnerUUID();
                if (ownerUUIDStr == null || ownerUUIDStr.isEmpty()) return true;
                UUID ownerUUID = UUID.fromString(ownerUUIDStr);

                if (ownerUUID.equals(uuid)) {
                    sp.sendMessage(Text.literal("§c✗ No puedes destruir tu propio Núcleo."), false);
                    return false;
                }

                if (sw.getServer().getPlayerManager().getPlayer(ownerUUID) == null) {
                    sp.sendMessage(Text.literal(
                        "§c✗ Este Núcleo no se puede destruir mientras su propietario está offline."), false);
                    return false;
                }

                sw.getServer().getPlayerManager().broadcast(
                    Text.literal("§4☠ §fEl Núcleo de §c" + be.getOwnerName() +
                        "§f fue destruido por §e" + sp.getName().getString() + "§f. ¡Eliminado!"), false);
                sm.eliminate(ownerUUID);
                ServerPlayerEntity owner = sw.getServer().getPlayerManager().getPlayer(ownerUUID);
                if (owner != null) {
                    owner.networkHandler.disconnect(
                        Text.literal("§4Tu Núcleo fue destruido. Partida terminada."));
                }
                return true;
            }

            if (blockId.equals(CLAIM_BLOCK_ID)) {
                UUID zoneOwner = sm.getZoneOwnerAt(pos.getX(), pos.getZ());
                if (zoneOwner != null && !zoneOwner.equals(uuid)) {
                    sp.sendMessage(Text.literal(
                        "§c✗ No puedes romper la zona de protección de otro jugador."), false);
                    return false;
                }
                return true;
            }

            UUID zoneOwner = sm.getZoneOwnerAt(pos.getX(), pos.getZ());
            if (zoneOwner != null && !zoneOwner.equals(uuid)) {
                String heldId = Registries.ITEM.getId(sp.getMainHandStack().getItem()).toString();
                if (heldId.equals(WARHAMMER_ID)) {
                    var stack = sp.getMainHandStack();
                    int newDamage = stack.getDamage() + 1;
                    if (newDamage >= stack.getMaxDamage()) {
                        stack.decrement(1);
                    } else {
                        stack.setDamage(newDamage);
                    }
                    return true;
                }
                sp.sendMessage(Text.literal(
                    "§c✗ Esta zona está protegida. Necesitas una §fMaza de Guerra§c para destruir aquí."), false);
                return false;
            }

            if (sm.getPhase(uuid) != 0) return true;

            if (isRestrictedOre(state)) {
                sp.sendMessage(Text.literal(
                    "§c✗ No puedes picar hierro, oro, diamante ni netherita hasta colocar tu Núcleo."), false);
                return false;
            }

            return true;
        });
    }
}
