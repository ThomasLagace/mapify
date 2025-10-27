package me.thomaslagace.mapify;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public interface IModCommand {
    void register(CommandDispatcher<ServerCommandSource> dispatcher,
                  CommandRegistryAccess commandRegistryAccess,
                  CommandManager.RegistrationEnvironment environment);
    int run(CommandContext<ServerCommandSource> ctx);
}
