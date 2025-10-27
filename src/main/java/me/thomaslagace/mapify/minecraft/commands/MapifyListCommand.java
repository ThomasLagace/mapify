package me.thomaslagace.mapify.minecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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

public class MapifyListCommand implements IModCommand {
    private final MinecraftImagesDatabase imagesDatabase;

    public MapifyListCommand(MinecraftImagesDatabase imagesDatabase) {
        this.imagesDatabase = imagesDatabase;
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher,
                         CommandRegistryAccess commandRegistryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal(Config.MAPIFY_ROOT_COMMAND)
            .then(CommandManager.literal(Config.MAPIFY_LIST_ARGUMENT_COMMAND)
            .executes(this::run)));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal(Config.MUST_BE_PLAYER));
            return 0;
        }

        List<MinecraftImage> allPlayerImages = imagesDatabase.getMinecraftImagesByCreatorId(player.getUuid());

        source.sendMessage(Text.literal(getFormatedMessage(allPlayerImages)));

        return Command.SINGLE_SUCCESS;
    }

    private static String getFormatedMessage(List<MinecraftImage> allPlayerImages) {
        StringBuilder message = new StringBuilder();

        message.append("--------------------\n");

        for (int i = 0; i < allPlayerImages.size(); i++) {
            MinecraftImage image = allPlayerImages.get(i);
            message.append(String.format("%d: %s\n", i + 1, image.getName()));
        }

        if (allPlayerImages.isEmpty()) {
            message.append(Config.CURRENTLY_NO_IMAGES);
        }

        message.insert(0, String.format(Config.CURRENTLY_IMAGES_COUNT, allPlayerImages.size(), Config.MAX_MAPS_COUNT_BY_USER) + "\n");
        return message.toString();
    }
}
