package me.thomaslagace.mapify.events.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.UUID;
import me.thomaslagace.mapify.map.MapImage;
import me.thomaslagace.mapify.map.MapsManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;


/**
 * Class that handles the /mapify command.
 */
public class MapifyCommand {
    private static final String commandString = "mapify";
    private static final String createCommandString = "create";
    private static final String nameArgumentString = "name";
    private static final String urlArgumentString = "url";

    /**
     * Registers the /mapify command.
     */
    public static void register() {
        CommandRegistrationCallback.EVENT.register(MapifyCommand::listener);
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

        dispatcher.register(CommandManager.literal(commandString)
                // /mapify create <width> <height> <name> <url>
                .then(CommandManager.literal(createCommandString)
                        .then(CommandManager.argument("width", IntegerArgumentType.integer())
                                .then(CommandManager.argument("height", IntegerArgumentType.integer())
                                        .then(CommandManager.argument(nameArgumentString, StringArgumentType.string())
                                                .then(CommandManager.argument(urlArgumentString, StringArgumentType.greedyString())
                                                        .executes(MapifyCommand::create))))))
                // /mapify get <name>
                .then(CommandManager.literal("get")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) {
                                        return builder.buildFuture();
                                    }

                                    MapsManager.getMapsImages().forEach(map -> {
                                        if (map.getCreatorId() == player.getUuid()) {
                                            builder.suggest(map.getName());
                                        }
                                    });
                                    return builder.buildFuture();
                                })
                                .executes(MapifyCommand::get))));
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
            source.sendError(Text.literal("You must be a player to run this command"));
            return 0;
        }

        URL url;
        try {
            URI uri = new URI(StringArgumentType.getString(context, urlArgumentString));
            url = uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            source.sendError(Text.literal("Invalid URL"));
            return 0;
        }

        source.sendMessage(Text.literal("Creating map..."));

        UUID playerId = source.getPlayer().getUuid();
        String name = StringArgumentType.getString(context, nameArgumentString);
        int width = IntegerArgumentType.getInteger(context, "width");
        int height = IntegerArgumentType.getInteger(context, "height");

        MapIdComponent[][] minecraftMapIds = new MapIdComponent[width][height];

        for (int i = 0; i < minecraftMapIds.length; i++) {
            for (int j = 0; j < minecraftMapIds[i].length; j++) {
                minecraftMapIds[i][j] = source.getWorld().increaseAndGetMapId();
            }
        }

        MapImage mapImage;
        try {
            mapImage = new MapImage(playerId, name, url, width, height, minecraftMapIds);
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal(e.getMessage()));
            return 0;
        }

        try {
            mapImage.fetchAndStoreImage();
        } catch (Exception e) {
            source.sendError(Text.literal("Failed to fetch and store image"));
            return 0;
        }

        try {
            MapsManager.add(mapImage);
        } catch (IllegalArgumentException e) {
            source.sendError(Text.literal(e.getMessage()));
            return 0;
        }
        source.sendMessage(Text.literal("Map created! The name of the map is " + name));

        mapImage.assignMapStateWithImage(source.getWorld());

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
            source.sendError(Text.literal("You must be a player to run this command"));
            return 0;
        }

        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();

        String name = StringArgumentType.getString(context, "name");

        List<MapImage> allMapsImage = MapsManager.getMapsImages();

        MapImage mapImage = null;
        for (MapImage map : allMapsImage) {
            if (map.getName().equals(name) && map.getCreatorId().equals(player.getUuid())) {
                mapImage = map;
                break;
            }
        }

        if (mapImage == null) {
            source.sendError(Text.literal("Map not found"));
            return 0;
        }

        ItemStack[] itemStacks = mapImage.getMapsAsItemStacks();

        for (ItemStack itemStack : itemStacks) {
            player.getInventory().insertStack(itemStack);
        }

        return Command.SINGLE_SUCCESS;
    }
}
