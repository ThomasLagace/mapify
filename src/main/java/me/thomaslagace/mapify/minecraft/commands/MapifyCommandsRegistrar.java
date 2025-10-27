package me.thomaslagace.mapify.minecraft.commands;

import java.util.List;
import me.thomaslagace.mapify.IModCommand;
import me.thomaslagace.mapify.Mapify;
import me.thomaslagace.mapify.ModRegisterable;
import me.thomaslagace.mapify.minecraft.MinecraftImagesDatabase;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class MapifyCommandsRegistrar implements ModRegisterable {
    @Override
    public void register() {
        MinecraftImagesDatabase imagesDatabase = Mapify.minecraftImagesDatabase;
        List<IModCommand> commands = List.of(
            new MapifyCreateCommand(imagesDatabase),
            new MapifyGetCommand(imagesDatabase),
            new MapifyListCommand(imagesDatabase),
            new MapifyRemoveCommand(imagesDatabase)
        );

        commands.forEach(command -> {
            CommandRegistrationCallback.EVENT.register(command::register);
        });
    }
}
