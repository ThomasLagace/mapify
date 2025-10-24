package me.thomaslagace.mapify.minecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import me.thomaslagace.mapify.ModRegisterable;
import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.image.ImageUtil;
import me.thomaslagace.mapify.minecraft.MinecraftImage;
import me.thomaslagace.mapify.minecraft.MinecraftImagesDatabase;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * Class that handles the /mapify command.
 */
public class MapifyCommand implements ModRegisterable {
    private final MinecraftImagesDatabase imagesDatabase;

    public MapifyCommand(MinecraftImagesDatabase imagesDatabase) {
        this.imagesDatabase = imagesDatabase;
    }

    public void register() {
        CommandRegistrationCallback.EVENT.register(this::listener);
    }

    private CompletableFuture<Suggestions> suggestImages(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            return builder.buildFuture();
        }

        for (MinecraftImage map : imagesDatabase.getMinecraftImagesByCreatorId(player.getUuid())) {
            builder.suggest(map.getName());
        }

        return builder.buildFuture();
    }

    /**
     * Listener for the /mapify command.
     *
     * @param dispatcher The command dispatcher.
     * @param registryAccess The command registry access.
     * @param environment The command registration environment.
     */
    private void listener(CommandDispatcher<ServerCommandSource> dispatcher,
                                 CommandRegistryAccess registryAccess,
                                 CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal(Config.MAPIFY_ROOT_COMMAND)
                // /mapify get <name>
                .then(CommandManager.literal(Config.MAPIFY_GET_ARGUMENT_COMMAND)
                        .then(CommandManager.argument(Config.MAPIFY_NAME_ARGUMENT_COMMAND, StringArgumentType.string())
                                .suggests(this::suggestImages)
                                .executes(this::get)))
                // /mapify list
                .then(CommandManager.literal(Config.MAPIFY_LIST_ARGUMENT_COMMAND)
                        .executes(this::list))
                // /mapify remove <name>
                .then(CommandManager.literal(Config.MAPIFY_REMOVE_ARGUMENT_COMMAND)
                        .then(CommandManager.argument(Config.MAPIFY_NAME_ARGUMENT_COMMAND, StringArgumentType.string())
                                .suggests(this::suggestImages)
                                .executes(this::remove))));
    }

    /**
     * Listener for the /mapify get command.
     *
     * @param context The command context.
     * @return The result of the command.
     */
    private int get(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendError(Text.literal(Config.MUST_BE_PLAYER));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        MinecraftImageManager imageManagerInstance = MinecraftImageManager.getInstance();

        String name = StringArgumentType.getString(context, Config.MAPIFY_NAME_ARGUMENT_COMMAND);

        List<MinecraftImage> allImages = imageManagerInstance.getImagesByPlayerId(player.getUuid());

        MinecraftImage mapImage = null;
        for (MinecraftImage map : allImages) {
            if (map.getImageGetter().getName().equals(name)) {
                mapImage = map;
                break;
            }
        }

        if (mapImage == null) {
            source.sendError(Text.literal(Config.IMAGE_NOT_FOUND));
            return 0;
        }

        ItemStack paper = createMapItemStack(mapImage, source);

        player.getInventory().insertStack(paper);

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Listener for the /mapify list command.
     *
     * @param context The command context.
     * @return The result of the command.
     */
    public int list(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendError(Text.literal(Config.MUST_BE_PLAYER));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        MinecraftImageManager imageManagerInstance = MinecraftImageManager.getInstance();

        List<MinecraftImage> allPlayerImages = imageManagerInstance.getImagesByPlayerId(player.getUuid());

        StringBuilder message = new StringBuilder();

        message.append("--------------------\n");

        for (int i = 0; i < allPlayerImages.size(); i++) {
            MinecraftImage map = allPlayerImages.get(i);
            message.append(String.format("%d: %s\n", i + 1, map.getImageGetter().getName()));
        }

        if (allPlayerImages.isEmpty()) {
            message.append(Config.CURRENTLY_NO_IMAGES);
        }

        message.insert(0, String.format(Config.CURRENTLY_IMAGES_COUNT, allPlayerImages.size(), Config.MAX_MAPS_COUNT_BY_USER) + "\n");

        source.sendMessage(Text.literal(message.toString()));

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Listener for the /mapify remove command.
     *
     * @param context The command context.
     * @return The result of the command.
     */
    public int remove(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendError(Text.literal(Config.MUST_BE_PLAYER));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        MinecraftImageManager imageManagerInstance = MinecraftImageManager.getInstance();

        String name = StringArgumentType.getString(context, Config.MAPIFY_NAME_ARGUMENT_COMMAND);

        List<MinecraftImage> allMapsImage = imageManagerInstance.getImagesByPlayerId(player.getUuid());
        for (MinecraftImage map : allMapsImage) {
            if (map.getImageGetter().getName().equals(name)) {
                imageManagerInstance.remove(map);
                source.sendMessage(Text.literal(Config.IMAGE_REMOVED));
                return Command.SINGLE_SUCCESS;
            }
        }

        source.sendError(Text.literal(Config.IMAGE_NOT_FOUND));
        return 0;
    }
}
