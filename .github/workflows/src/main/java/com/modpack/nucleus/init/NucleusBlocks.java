package com.modpack.nucleus.init;

import com.modpack.nucleus.NucleusMod;
import com.modpack.nucleus.block.NucleusBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class NucleusBlocks {

    public static final NucleusBlock NUCLEUS_BLOCK = new NucleusBlock(
        AbstractBlock.Settings.create()
            .hardness(50f)
            .resistance(1200f)
            .requiresTool()
            .luminance(state -> 6)
    );

    public static final BlockItem NUCLEUS_BLOCK_ITEM = new BlockItem(
        NUCLEUS_BLOCK,
        new Item.Settings().maxCount(1)
    );

    public static void register() {
        Registry.register(Registries.BLOCK, NucleusMod.id("nucleus_block"), NUCLEUS_BLOCK);
        Registry.register(Registries.ITEM,  NucleusMod.id("nucleus_block"), NUCLEUS_BLOCK_ITEM);
        NucleusMod.LOGGER.info("[Nucleus] Bloque Núcleo registrado.");
    }
}
