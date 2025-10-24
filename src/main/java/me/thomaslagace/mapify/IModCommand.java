package me.thomaslagace.mapify;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

public interface IModCommand {
    List<ArgumentBuilder<ServerCommandSource,? extends ArgumentBuilder<?,?>>> getArguments();
    int run(CommandContext<ServerCommandSource> ctx);
}
