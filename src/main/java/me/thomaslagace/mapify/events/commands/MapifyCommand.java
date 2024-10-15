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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import me.thomaslagace.mapify.Image;
import me.thomaslagace.mapify.ImageMinecraft;
import me.thomaslagace.mapify.Mapify;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.MapIdComponent;
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

        dispatcher.register(CommandManager.literal("mapfiy")
                // /mapify create <width> <height> <name> <url>
                .then(CommandManager.literal("create")
                        .then(CommandManager.argument("width", IntegerArgumentType.integer())
                                .then(CommandManager.argument("height", IntegerArgumentType.integer())
                                        .then(CommandManager.argument("name", StringArgumentType.string())
                                                .then(CommandManager.argument("url", StringArgumentType.greedyString())
                                                        .executes(MapifyCommand::create))))))
                // /mapify get <name>
                .then(CommandManager.literal("get")
                        .then(CommandManager.argument("name", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    ServerPlayerEntity player = context.getSource().getPlayer();
                                    if (player == null) {
                                        return builder.buildFuture();
                                    }

                                    Mapify.imageManager.getImages().forEach(map -> {
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
            URI uri = new URI(StringArgumentType.getString(context, "url"));
            url = uri.toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            source.sendError(Text.literal("Invalid URL"));
            return 0;
        }

        source.sendMessage(Text.literal("Creating map..."));

        UUID playerId = source.getPlayer().getUuid();
        String name = StringArgumentType.getString(context, "name");
        int width = IntegerArgumentType.getInteger(context, "width");
        int height = IntegerArgumentType.getInteger(context, "height");

        Image image = new Image(playerId, name, url);
        ImageMinecraft imageMinecraft = new ImageMinecraft(Mapify.imageManager.getNextId(), playerId,name, image, width, height);

        imageMinecraft.reserveMapIds(source.getWorld());
        imageMinecraft.assignMapStateToWorld(source.getWorld());

        Mapify.imageManager.add(imageMinecraft);

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

        List<ImageMinecraft> allMapsImage = Mapify.imageManager.getImages();

        ImageMinecraft mapImage = null;
        for (ImageMinecraft map : allMapsImage) {
            if (map.getName().equals(name)
                && map.getCreatorId().equals(player.getUuid())) {
                mapImage = map;
                break;
            }
        }

        if (mapImage == null) {
            source.sendError(Text.literal("Map not found"));
            return 0;
        }

        ItemStack paper = new ItemStack(Items.PAPER, 1);

        Text paperName = Text.of(String.format("%s %s x %s", mapImage.getName(), mapImage.getWidth(), mapImage.getHeight()));
        paper.set(DataComponentTypes.ITEM_NAME, paperName);

        String creatorName = Objects.requireNonNull(source.getServer().getPlayerManager().getPlayer(mapImage.getCreatorId())).getName().getString();

        LoreComponent loreComponent = new LoreComponent(new ArrayList<>());
        loreComponent.lines().add(Text.of(mapImage.getName()));
        loreComponent.lines().add(Text.of(mapImage.getImage().getUrl().toString()));
        loreComponent.lines().add(Text.of("Created by " + creatorName));
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

        player.getInventory().insertStack(paper);

        return Command.SINGLE_SUCCESS;
    }
}
