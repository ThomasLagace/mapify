package me.thomaslagace.mapify.minecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.image.DomainNotAllowedException;
import me.thomaslagace.mapify.image.ImageUtil;
import me.thomaslagace.mapify.image.InvalidImageException;
import me.thomaslagace.mapify.image.OnDiskImage;
import me.thomaslagace.mapify.minecraft.MinecraftImage;
import me.thomaslagace.mapify.minecraft.MinecraftImageManager;
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
public final class MapifyCommand {
    private MapifyCommand() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Registers the /mapify command.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(MapifyCommand::listener);
    }

    private static CompletableFuture<Suggestions> suggestImages(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        if (player == null) {
            return builder.buildFuture();
        }

        for (MinecraftImage map : MinecraftImageManager.getInstance().getImagesByPlayerId(player.getUuid())) {
            builder.suggest(map.getImage().getName());
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
    private static void listener(CommandDispatcher<ServerCommandSource> dispatcher,
                                 CommandRegistryAccess registryAccess,
                                 CommandManager.RegistrationEnvironment environment) {

        dispatcher.register(CommandManager.literal(Config.MAPIFY_ROOT_COMMAND)
                // /mapify create <width> <height> <name> <url>
                .then(CommandManager.literal(Config.MAPIFY_CREATE_ARGUMENT_COMMAND)
                        .then(CommandManager.argument(Config.MAPIFY_WIDTH_ARGUMENT_COMMAND, IntegerArgumentType.integer())
                                .then(CommandManager.argument(Config.MAPIFY_HEIGHT_ARGUMENT_COMMAND, IntegerArgumentType.integer())
                                        .then(CommandManager.argument(Config.MAPIFY_NAME_ARGUMENT_COMMAND, StringArgumentType.string())
                                                .then(CommandManager.argument(Config.MAPIFY_URL_ARGUMENT_COMMAND, StringArgumentType.greedyString())
                                                        .executes(MapifyCommand::create))))))
                // /mapify get <name>
                .then(CommandManager.literal(Config.MAPIFY_GET_ARGUMENT_COMMAND)
                        .then(CommandManager.argument(Config.MAPIFY_NAME_ARGUMENT_COMMAND, StringArgumentType.string())
                                .suggests(MapifyCommand::suggestImages)
                                .executes(MapifyCommand::get)))
                // /mapify list
                .then(CommandManager.literal(Config.MAPIFY_LIST_ARGUMENT_COMMAND)
                        .executes(MapifyCommand::list))
                // /mapify remove <name>
                .then(CommandManager.literal(Config.MAPIFY_REMOVE_ARGUMENT_COMMAND)
                        .then(CommandManager.argument(Config.MAPIFY_NAME_ARGUMENT_COMMAND, StringArgumentType.string())
                                .suggests(MapifyCommand::suggestImages)
                                .executes(MapifyCommand::remove))));
    }

    /**
     * Listener for the /mapify create command.
     *
     * @param context The command context.
     * @return The result of the command.
     */
    private static int create(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendError(Text.literal(Config.MUST_BE_PLAYER));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        MinecraftImageManager imageManagerInstance = MinecraftImageManager.getInstance();

        int mapCount = imageManagerInstance.getImagesByPlayerId(player.getUuid()).size();

        if (mapCount >= Config.MAX_MAPS_COUNT_BY_USER) {
            source.sendError(Text.literal(Config.USER_MAP_LIMIT_REACHED));
            return 0;
        }

        source.sendMessage(Text.literal(Config.CREATING_IMAGE));

        String urlArg = StringArgumentType.getString(context, Config.MAPIFY_URL_ARGUMENT_COMMAND);
        String nameArg = StringArgumentType.getString(context, Config.MAPIFY_NAME_ARGUMENT_COMMAND);
        int widthArg = IntegerArgumentType.getInteger(context, Config.MAPIFY_WIDTH_ARGUMENT_COMMAND);
        int heightArg = IntegerArgumentType.getInteger(context, Config.MAPIFY_HEIGHT_ARGUMENT_COMMAND);

        if (widthArg > Config.MAX_MAPS_WIDTH || heightArg > Config.MAX_MAPS_HEIGHT) {
            source.sendError(Text.literal(Config.IMAGE_IS_TO_LARGE));
            return 0;
        }

        if (imageManagerInstance.getImagesByPlayerId(player.getUuid()).stream().anyMatch(image -> image.getImage().getName().equals(nameArg))) {
            source.sendError(Text.literal(Config.IMAGE_ALREADY_EXISTS));
            return 0;
        }

        BufferedImage image;
        try {
            image = ImageUtil.getImageFromURL(urlArg, Config.ALLOWED_IMAGE_DOMAINS, Config.MAX_IMAGE_SIZE);
        } catch (InvalidImageException e) {
            source.sendError(Text.literal(Config.URL_IS_NOT_AN_IMAGE));
            return 0;
        } catch (DomainNotAllowedException e) {
            source.sendError(Text.literal(Config.INVALID_DOMAIN));
            return 0;
        } catch (IOException e) {
            source.sendError(Text.literal(Config.ERROR_MESSAGE));
            return 0;
        } catch (URISyntaxException e) {
            source.sendError(Text.literal(Config.INVALID_URL));
            return 0;
        }

        OnDiskImage onDiskImage;
        try {
            Path path = Paths.get(Config.STORAGE_PATH + "images/" + player.getUuid());
            onDiskImage = new OnDiskImage(nameArg, path, image);
        } catch (IOException e) {
            source.sendError(Text.literal(Config.ERROR_MESSAGE));
            return 0;
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal(Config.INVALID_IMAGE_NAME));
            return 0;
        }

        MinecraftImage minecraftImage = new MinecraftImage(imageManagerInstance.getNextId(), player.getUuid(), onDiskImage, widthArg, heightArg);

        minecraftImage.reserveMapIds(source.getWorld(), imageManagerInstance.getUnusedMapIds());
        try {
            minecraftImage.assignMapStateToWorld(source.getWorld());
        } catch (IOException e) {
            source.sendError(Text.literal(Config.ERROR_MESSAGE));
            return 0;
        }

        imageManagerInstance.add(minecraftImage);
        source.sendMessage(Text.literal(Config.IMAGE_CREATED));

        player.getInventory().insertStack(createMapItemStack(minecraftImage, source));

        return Command.SINGLE_SUCCESS;
    }

    /**
     * Listener for the /mapify get command.
     *
     * @param context The command context.
     * @return The result of the command.
     */
    private static int get(CommandContext<ServerCommandSource> context) {
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
            if (map.getImage().getName().equals(name)) {
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
    public static int list(CommandContext<ServerCommandSource> context) {
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
            message.append(String.format("%d: %s\n", i + 1, map.getImage().getName()));
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
    public static int remove(CommandContext<ServerCommandSource> context) {
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
            if (map.getImage().getName().equals(name)) {
                imageManagerInstance.remove(map);
                source.sendMessage(Text.literal(Config.IMAGE_REMOVED));
                return Command.SINGLE_SUCCESS;
            }
        }

        source.sendError(Text.literal(Config.IMAGE_NOT_FOUND));
        return 0;
    }

    /**
     * Creates a map item stack from an image minecraft.
     *
     * @param mapImage The image minecraft.
     * @return The map paper item stack.
     */
    public static ItemStack createMapItemStack(MinecraftImage mapImage, ServerCommandSource source) {
        ItemStack paper = new ItemStack(Items.PAPER, 1);

        Text paperName = Text.of(String.format("%s %s x %s", mapImage.getImage().getName(), mapImage.getWidth(), mapImage.getHeight()));
        paper.set(DataComponentTypes.ITEM_NAME, paperName);

        String creatorName = Objects.requireNonNull(source.getServer().getPlayerManager().getPlayer(mapImage.getCreatorId())).getName().getString();

        LoreComponent loreComponent = new LoreComponent(new ArrayList<>());
        loreComponent.lines().add(Text.of(mapImage.getImage().getName()));
        loreComponent.lines().add(Text.of(Config.CREATED_BY + creatorName));
        paper.set(DataComponentTypes.LORE, loreComponent);

        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("mapify", true);
        nbtCompound.putInt("mapify.id", mapImage.getId());
        nbtCompound.putUuid("mapify.creatorId", mapImage.getCreatorId());
        nbtCompound.putString("mapify.name", mapImage.getImage().getName());
        nbtCompound.putInt("mapify.width", mapImage.getWidth());
        nbtCompound.putInt("mapify.height", mapImage.getHeight());

        NbtComponent nbt = NbtComponent.of(nbtCompound);
        paper.set(DataComponentTypes.CUSTOM_DATA, nbt);

        return paper;
    }
}
