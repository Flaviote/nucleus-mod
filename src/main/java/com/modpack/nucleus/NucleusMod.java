package com.modpack.nucleus;

import com.modpack.nucleus.events.BlockEvents;
import com.modpack.nucleus.events.LobbyEvents;
import com.modpack.nucleus.events.PlayerEvents;
import com.modpack.nucleus.events.RadarEvents;
import com.modpack.nucleus.init.NucleusBlockEntities;
import com.modpack.nucleus.init.NucleusBlocks;
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

        LobbyEvents.register();
        BlockEvents.register();
        PlayerEvents.register();
        RadarEvents.register();

        PolymerResourcePackUtils.markAsRequired();

        LOGGER.info("[Nucleus] Mod v2 inicializado — lógica integrada sin KubeJS.");
    }

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }
}
