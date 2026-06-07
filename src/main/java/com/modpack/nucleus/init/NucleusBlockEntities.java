package com.modpack.nucleus.init;

import com.modpack.nucleus.NucleusMod;
import com.modpack.nucleus.entity.NucleusBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class NucleusBlockEntities {

    public static BlockEntityType<NucleusBlockEntity> NUCLEUS_BLOCK_ENTITY;

    public static void register() {
        NUCLEUS_BLOCK_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            NucleusMod.id("nucleus_block_entity"),
            BlockEntityType.Builder
                .create(NucleusBlockEntity::new, NucleusBlocks.NUCLEUS_BLOCK)
                .build()
        );
        NucleusMod.LOGGER.info("[Nucleus] BlockEntity registrado.");
    }
}
