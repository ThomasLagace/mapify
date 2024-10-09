package me.thomaslagace.mapify;

import me.thomaslagace.mapify.events.commands.MapifyCommand;
import me.thomaslagace.mapify.events.rightclick.ItemFrameRightClickHandler;
import me.thomaslagace.mapify.map.MapsManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The main class of the mapify mod.
 */
public class Mapify implements ModInitializer {
    public static final String MOD_ID = "mapify";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        MapsManager.initialize("./mapify/maps.json");

        MapifyCommand.register();
        ItemFrameRightClickHandler.register();

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            onDisable();
        });
    }

    /**
     * Called when the server is stopping.
     */
    public void onDisable() {
        MapsManager.serialize("./mapify/maps.json");
    }
}