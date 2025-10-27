package me.thomaslagace.mapify;

import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.minecraft.rightclick.ItemFrameRightClickHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        List<ModRegisterable> registerables = List.of(
            new MapifyCommand(),
            new ItemFrameRightClickHandler()
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
            MinecraftImageManager.getInstance().serialize(Config.STORAGE_PATH);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize the Minecraft images.", e);
        }
    }
}
