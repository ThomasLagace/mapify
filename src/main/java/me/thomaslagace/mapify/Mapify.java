package me.thomaslagace.mapify;

import me.thomaslagace.mapify.events.commands.MapifyCommand;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mapify implements ModInitializer {
    public static final String MOD_ID = "mapify";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world!");

        MapifyCommand.register();
    }
}