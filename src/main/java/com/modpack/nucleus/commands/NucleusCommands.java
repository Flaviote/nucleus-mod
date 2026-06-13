package com.modpack.nucleus.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class NucleusCommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(literal("nucleus")
            .requires(source -> source.hasPermissionLevel(4))
            .then(literal("setborder")
                .then(argument("radio", IntegerArgumentType.integer(50, 5000))
                    .executes(ctx -> {
                        int radio = IntegerArgumentType.getInteger(ctx, "radio");
                        ServerCommandSource source = ctx.getSource();

                        source.getServer().getOverworld().getWorldBorder().setCenter(0, 0);
                        source.getServer().getOverworld().getWorldBorder().setSize(radio * 2);

                        source.sendFeedback(() -> Text.literal(
                            "§a✔ Borde de mundo establecido: radio §e" + radio +
                            "§a bloques (mapa " + (radio * 2) + "x" + (radio * 2) + ")."), true);

                        return 1;
                    })
                )
            )
            .then(literal("border")
                .then(literal("recomendado")
                    .then(argument("jugadores", IntegerArgumentType.integer(1, 100))
                        .executes(ctx -> {
                            int jugadores = IntegerArgumentType.getInteger(ctx, "jugadores");
                            int radio = recommendedRadius(jugadores);
                            ServerCommandSource source = ctx.getSource();

                            source.getServer().getOverworld().getWorldBorder().setCenter(0, 0);
                            source.getServer().getOverworld().getWorldBorder().setSize(radio * 2);

                            source.sendFeedback(() -> Text.literal(
                                "§a✔ Borde recomendado para §e" + jugadores + " §ajugadores: radio §e" +
                                radio + "§a (mapa " + (radio * 2) + "x" + (radio * 2) + ")."), true);

                            return 1;
                        })
                    )
                )
            )
        );
    }

    private static int recommendedRadius(int players) {
        if (players <= 10) return 400;
        if (players <= 15) return 600;
        if (players <= 20) return 750;
        return 1000;
    }
}
