package com.modpack.nucleus;

import com.modpack.nucleus.init.NucleusBlocks;
import com.modpack.nucleus.init.NucleusBlockEntities;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NucleusMod implements ModInitializer {

    public static final String MOD_ID = "nucleus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        NucleusBlocks.register();
        NucleusBlockEntities.register();

        // Polymer generará y servirá el resourcepack automáticamente
        PolymerResourcePackUtils.markAsRequired();

        LOGGER.info("[Nucleus] Mod inicializado correctamente.");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
