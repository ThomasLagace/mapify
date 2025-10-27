package me.thomaslagace.mapify.minecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.thomaslagace.mapify.IModCommand;
import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.minecraft.MinecraftImage;
import me.thomaslagace.mapify.minecraft.MinecraftImagesDatabase;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.List;

public class MapifyRemoveCommand implements IModCommand {
    private final MinecraftImagesDatabase imagesDatabase;

    public MapifyRemoveCommand(MinecraftImagesDatabase imagesDatabase) {
        this.imagesDatabase = imagesDatabase;
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal(Config.MAPIFY_ROOT_COMMAND)
            .then(CommandManager.literal(Config.MAPIFY_REMOVE_ARGUMENT_COMMAND)
            .then(CommandManager.argument(Config.MAPIFY_NAME_ARGUMENT_COMMAND, StringArgumentType.string())
                .suggests((context, builder) ->
                        MapifyImageSuggestion.suggestImages(context, builder, imagesDatabase))
            .executes(this::run))));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal(Config.MUST_BE_PLAYER));
            return 0;
        }

        String name = StringArgumentType.getString(ctx, Config.MAPIFY_NAME_ARGUMENT_COMMAND);

        MinecraftImage image = imagesDatabase.getMinecraftImageByNameAndCreatorId(name, player.getUuid());

        if (image == null) {
            source.sendError(Text.literal(Config.IMAGE_NOT_FOUND));
            return 0;
        }

        imagesDatabase.removeMinecraftImage(image);

        return Command.SINGLE_SUCCESS;
    }
}
