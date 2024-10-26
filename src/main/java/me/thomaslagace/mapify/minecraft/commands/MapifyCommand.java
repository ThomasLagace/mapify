package me.thomaslagace.mapify.minecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.image.Image;
import me.thomaslagace.mapify.minecraft.MinecraftImage;
import me.thomaslagace.mapify.Mapify;
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
public class MapifyCommand {
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

        Mapify.imageManager.getImages().forEach(map -> {
            if (map.getCreatorId().equals(player.getUuid())) {
                builder.suggest(map.getName());
            }
        });
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

        URL url;
        try {
            URI uri = new URI(StringArgumentType.getString(context, Config.MAPIFY_URL_ARGUMENT_COMMAND));
            url = uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            source.sendError(Text.literal(Config.INVALID_URL));
            return 0;
        }

        if (!Config.ALLOWED_IMAGE_DOMAINS.isEmpty()
                && Config.ALLOWED_IMAGE_DOMAINS.stream().noneMatch(url.getHost()::contains)) {
            source.sendError(Text.literal(Config.INVALID_DOMAIN));
            return 0;
        }

        int mapCount = 0;
        for (MinecraftImage minecraftImage : Mapify.imageManager.getImages()) {
            if (minecraftImage.getCreatorId().equals(source.getPlayer().getUuid())) {
                mapCount++;
                if (minecraftImage.getName().equals(StringArgumentType.getString(context, Config.MAPIFY_NAME_ARGUMENT_COMMAND))) {
                    source.sendError(Text.literal(Config.IMAGE_ALREADY_EXISTS));
                    return 0;
                }
            }
        }

        if (mapCount >= Config.MAX_MAPS_COUNT_BY_USER) {
            source.sendError(Text.literal(Config.USER_MAP_LIMIT_REACHED));
            return 0;
        }

        source.sendMessage(Text.literal(Config.CREATING_IMAGE));

        UUID playerId = source.getPlayer().getUuid();
        String name = StringArgumentType.getString(context, Config.MAPIFY_NAME_ARGUMENT_COMMAND);
        int width = IntegerArgumentType.getInteger(context, Config.MAPIFY_WIDTH_ARGUMENT_COMMAND);
        int height = IntegerArgumentType.getInteger(context, Config.MAPIFY_HEIGHT_ARGUMENT_COMMAND);

        Image image = new Image(playerId, name, url);
        MinecraftImage minecraftImage = new MinecraftImage(Mapify.imageManager.getNextId(), playerId, name, image, width, height);

        minecraftImage.reserveMapIds(source.getWorld());
        minecraftImage.assignMapStateToWorldAsync(source.getWorld());

        Mapify.imageManager.add(minecraftImage);
        source.sendMessage(Text.literal(Config.IMAGE_CREATED));

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
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

        String name = StringArgumentType.getString(context, Config.MAPIFY_NAME_ARGUMENT_COMMAND);

        List<MinecraftImage> allMapsImage = Mapify.imageManager.getImages();

        MinecraftImage mapImage = null;
        for (MinecraftImage map : allMapsImage) {
            if (map.getName().equals(name)
                && map.getCreatorId().equals(player.getUuid())) {
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

        List<MinecraftImage> allMapsImage = Mapify.imageManager.getImages();

        int imageCount = 0;
        StringBuilder message = new StringBuilder();
        message.append("--------------------\n");
        for (MinecraftImage map : allMapsImage) {
            if (map.getCreatorId().equals(player.getUuid())) {
                message.append(String.format("%d : %s %d x %d", imageCount + 1, map.getName(), map.getWidth(), map.getHeight())).append("\n");
                imageCount++;
            }
        }

        if (imageCount <= 0) {
            message.append(Config.CURRENTLY_NO_IMAGES);
        }

        message.insert(0, String.format(Config.CURRENTLY_IMAGES_COUNT, imageCount, Config.MAX_MAPS_COUNT_BY_USER) + "\n");

        source.sendMessage(Text.literal(message.toString()));

        return Command.SINGLE_SUCCESS;
    }

    public static int remove(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity)) {
            source.sendError(Text.literal(Config.MUST_BE_PLAYER));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        String name = StringArgumentType.getString(context, Config.MAPIFY_NAME_ARGUMENT_COMMAND);

        List<MinecraftImage> allMapsImage = Mapify.imageManager.getImages();
        for (MinecraftImage map : allMapsImage) {
            if (map.getName().equals(name)
                && map.getCreatorId().equals(player.getUuid())) {

                Mapify.imageManager.remove(map);
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

        Text paperName = Text.of(String.format("%s %s x %s", mapImage.getName(), mapImage.getWidth(), mapImage.getHeight()));
        paper.set(DataComponentTypes.ITEM_NAME, paperName);

        String creatorName = Objects.requireNonNull(source.getServer().getPlayerManager().getPlayer(mapImage.getCreatorId())).getName().getString();

        LoreComponent loreComponent = new LoreComponent(new ArrayList<>());
        loreComponent.lines().add(Text.of(mapImage.getName()));
        loreComponent.lines().add(Text.of(mapImage.getImage().getUrl().toString()));
        loreComponent.lines().add(Text.of(Config.CREATED_BY + creatorName));
        paper.set(DataComponentTypes.LORE, loreComponent);

        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("mapify", true);
        nbtCompound.putInt("mapify.id", mapImage.getId());
        nbtCompound.putUuid("mapify.creatorId", mapImage.getCreatorId());
        nbtCompound.putString("mapify.name", mapImage.getName());
        nbtCompound.putInt("mapify.width", mapImage.getWidth());
        nbtCompound.putInt("mapify.height", mapImage.getHeight());

        NbtComponent nbt = NbtComponent.of(nbtCompound);
        paper.set(DataComponentTypes.CUSTOM_DATA, nbt);

        return paper;
    }
}
