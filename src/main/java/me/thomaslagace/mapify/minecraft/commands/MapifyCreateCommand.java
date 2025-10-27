package me.thomaslagace.mapify.minecraft.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import me.thomaslagace.mapify.IModCommand;
import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.config.MapifyUtils;
import me.thomaslagace.mapify.image.OnDiskImageGetter;
import me.thomaslagace.mapify.image.UrlImageGetter;
import me.thomaslagace.mapify.minecraft.MinecraftImage;
import me.thomaslagace.mapify.minecraft.MinecraftImagesDatabase;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class MapifyCreateCommand implements IModCommand {
    private final MinecraftImagesDatabase imagesDatabase;

    public MapifyCreateCommand(MinecraftImagesDatabase imagesDatabase) {
        this.imagesDatabase = imagesDatabase;
    }

    @Override
    public void register(CommandDispatcher<ServerCommandSource> dispatcher,
                         CommandRegistryAccess commandRegistryAccess,
                         CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal(Config.MAPIFY_ROOT_COMMAND)
            .then(CommandManager.literal(Config.MAPIFY_CREATE_ARGUMENT_COMMAND)
            .then(CommandManager.argument(Config.MAPIFY_WIDTH_ARGUMENT_COMMAND, IntegerArgumentType.integer())
            .then(CommandManager.argument(Config.MAPIFY_HEIGHT_ARGUMENT_COMMAND, IntegerArgumentType.integer())
            .then(CommandManager.argument(Config.MAPIFY_NAME_ARGUMENT_COMMAND, StringArgumentType.string())
            .then(CommandManager.argument(Config.MAPIFY_URL_ARGUMENT_COMMAND, StringArgumentType.greedyString())
            .executes(this::run)))))));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal(Config.MUST_BE_PLAYER));
            return 0;
        }

        int mapCount = imagesDatabase.getMinecraftImagesByCreatorId(player.getUuid()).size();
        if (mapCount >= Config.MAX_MAPS_COUNT_BY_USER) {
            source.sendError(Text.literal(Config.USER_MAP_LIMIT_REACHED));
            return 0;
        }

        source.sendMessage(Text.literal(Config.CREATING_IMAGE));

        String urlArg = StringArgumentType.getString(ctx, Config.MAPIFY_URL_ARGUMENT_COMMAND);
        String nameArg = StringArgumentType.getString(ctx, Config.MAPIFY_NAME_ARGUMENT_COMMAND);
        int widthArg = IntegerArgumentType.getInteger(ctx, Config.MAPIFY_WIDTH_ARGUMENT_COMMAND);
        int heightArg = IntegerArgumentType.getInteger(ctx, Config.MAPIFY_HEIGHT_ARGUMENT_COMMAND);

        if (widthArg > Config.MAX_MAPS_WIDTH || heightArg > Config.MAX_MAPS_HEIGHT) {
            source.sendError(Text.literal(Config.IMAGE_IS_TO_LARGE));
            return 0;
        }

        if (imagesDatabase.userHasAlreadyThisImageName(player.getUuid(), nameArg)) {
            source.sendError(Text.literal(Config.IMAGE_ALREADY_EXISTS));
            return 0;
        }

        URL url;
        try {
            url = (new URI(urlArg)).toURL();
        } catch (Exception e) {
            return 0;
        }

        boolean isUrlAllowed = MapifyUtils.isUrlAllowed(url);

        if (!isUrlAllowed) {
            return 0;
        }

        UrlImageGetter imageGetter = new UrlImageGetter(url, Config.MAX_IMAGE_SIZE);

        BufferedImage image;
        try {
            image = imageGetter.getImage();
        } catch (Exception e) {
            return 0;
        }

        Path path = Paths.get(Config.STORAGE_PATH + "images/" + player.getUuid());
        File file = path.toFile();
        OnDiskImageGetter onDiskImageGetter = new OnDiskImageGetter(file);

        try {
            onDiskImageGetter.setImage(image);
        } catch (IOException e) {
            source.sendError(Text.literal(Config.ERROR_MESSAGE));
            return 0;
        }

        MinecraftImage minecraftImage = new MinecraftImage(UUID.randomUUID(), nameArg, player.getUuid(), onDiskImageGetter);

        if (!imagesDatabase.addAndReserveMinecraftImage(minecraftImage, widthArg, heightArg)) {
            return 0;
        }

        minecraftImage.assignMapsToWorld(source.getWorld());

        source.sendMessage(Text.literal(Config.IMAGE_CREATED));

        player.getInventory().insertStack(minecraftImage.createDataMapItem());

        return Command.SINGLE_SUCCESS;
    }
}
