package me.thomaslagace.mapify;

import java.util.ArrayList;
import java.util.List;
import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.minecraft.MinecraftImagesDatabase;
import me.thomaslagace.mapify.minecraft.commands.MapifyCommandsRegistrar;
import me.thomaslagace.mapify.minecraft.rightclick.ItemFrameRightClickHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class of the mapify mod.
 */
public class Mapify implements ModInitializer {
    /**
     * The id of the mod.
     */
    public static final String MOD_ID = "mapify";
    /**
     * The logger of the mod.
     */
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static MinecraftImagesDatabase minecraftImagesDatabase;

    @Override
    public void onInitialize() {
        minecraftImagesDatabase = new MinecraftImagesDatabase(new ArrayList<>(), new ArrayList<>());

        List<ModRegisterable> registerables = List.of(
            new MapifyCommandsRegistrar(),
            new ItemFrameRightClickHandler(minecraftImagesDatabase)
        );

        registerables.forEach(ModRegisterable::register);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            this.onDisable();
        });
    }

    /**
     * Called when the server is stopping.
     */
    public void onDisable() {
        try {
        } catch (Exception e) {
            LOGGER.error("Failed to serialize the Minecraft images.", e);
        }
    }
}
