package me.thomaslagace.mapify.events.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class MapifyCommand {
    private static final String commandString = "mapify";
    private static final String createCommandString = "create";
    private static final String setCommandString = "set";
    private static final String nameArgumentString = "name";
    private static final String urlArgumentString = "url";
    private static final String newNameArgumentString = "new_name";

    public static void register() {
        CommandRegistrationCallback.EVENT.register(MapifyCommand::listener);
    }

    private static void listener(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess, CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal(commandString)
                .then(CommandManager.literal(createCommandString)
                        .then(CommandManager.argument(nameArgumentString, StringArgumentType.string())
                                .then(CommandManager.argument(urlArgumentString, StringArgumentType.string())
                                        .executes(MapifyCommand::create))))
                .then(CommandManager.literal(setCommandString)
                        .then(CommandManager.argument(nameArgumentString, StringArgumentType.string())
                                .then(CommandManager.argument(newNameArgumentString, StringArgumentType.string())
                                        .executes(MapifyCommand::set)))));
    }

    private static int create(CommandContext<ServerCommandSource> context) {
        return Command.SINGLE_SUCCESS;
    }

    private static int set(CommandContext<ServerCommandSource> context) {
        return Command.SINGLE_SUCCESS;
    }
}
