package me.thomaslagace.mapify.events.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import me.thomaslagace.mapify.map.MapImage;
import me.thomaslagace.mapify.map.MapsManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
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
                        .then(CommandManager.argument(nameArgumentString, StringArgumentType.string())
                                .then(CommandManager.argument(urlArgumentString, StringArgumentType.greedyString())
                                        .executes(MapifyCommand::create))))
                // /mapify get <name>
                .then(CommandManager.literal("get")
                        .then(CommandManager.argument("name", StringArgumentType.string())
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

        MapsManager.add(new MapImage(playerId, name, url, 1, 1, new int[]{0}));
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Listener for the /mapify get command.
     *
     * @param context The command context.
     * @return The result of the command.
     */
    private static int get(CommandContext<ServerCommandSource> context) {
        return Command.SINGLE_SUCCESS;
    }
}
