package me.thomaslagace.mapify.minecraft.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import me.thomaslagace.mapify.minecraft.MinecraftImage;
import me.thomaslagace.mapify.minecraft.MinecraftImagesDatabase;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class MapifyImageSuggestion {
    public static CompletableFuture<Suggestions> suggestImages(CommandContext<ServerCommandSource> context,
                                                               SuggestionsBuilder builder,
                                                               MinecraftImagesDatabase imagesDatabase) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            return builder.buildFuture();
        }

        for (MinecraftImage map : imagesDatabase.getMinecraftImagesByCreatorId(player.getUuid())) {
            builder.suggest(map.getName());
        }

        return builder.buildFuture();
    }
}
