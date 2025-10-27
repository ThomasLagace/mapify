package me.thomaslagace.mapify;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
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
        try {
            File file = new File(Config.STORAGE_PATH + "mapify.json");
            minecraftImagesDatabase = MinecraftImagesDatabase.deserialize(Files.readString(file.toPath()));
        } catch (Exception e) {
            minecraftImagesDatabase = new MinecraftImagesDatabase(new ArrayList<>(), new ArrayList<>());
            LOGGER.error("Failed to load Minecraft IMAGES!", e);
        }

        List<IModRegistrable> registrables = List.of(
            new MapifyCommandsRegistrar(),
            new ItemFrameRightClickHandler(minecraftImagesDatabase)
        );

        registrables.forEach(IModRegistrable::register);

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            this.onDisable();
        });
    }

    /**
     * Called when the server is stopping.
     */
    public void onDisable() {
        try {
            File file = new File(Config.STORAGE_PATH + "mapify.json");
            if (!file.getParentFile().exists()) {
                if (!file.getParentFile().mkdirs()) {
                    throw new IOException("Failed to create the directory for serialisation: " + file.getParentFile());
                }
            }
            FileOutputStream fileStream = new FileOutputStream(file);
            String serializedString = minecraftImagesDatabase.serialize();

            fileStream.write(serializedString.getBytes());
            fileStream.close();
        } catch (Exception e) {
            LOGGER.error("Failed to serialize the Minecraft images.", e);
        }
    }
}
