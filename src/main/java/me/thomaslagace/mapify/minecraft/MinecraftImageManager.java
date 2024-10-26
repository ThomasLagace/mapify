package me.thomaslagace.mapify.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import me.thomaslagace.mapify.Mapify;
import me.thomaslagace.mapify.config.Config;
import net.minecraft.component.type.MapIdComponent;
import org.slf4j.Logger;

/**
 * Manages all the Minecraft images in the application.
 */
public class MinecraftImageManager {
    private static final Logger LOGGER = Mapify.LOGGER;
    private static final int MAX_MAPS_COUNT_BY_USER = Config.MAX_MAPS_COUNT_BY_USER;
    private static final String STORAGE_PATH = Config.STORAGE_PATH;

    private static MinecraftImageManager instance;

    private int nextId;
    private final List<MinecraftImage> MinecraftImages;
    private final List<MapIdComponent> unusedMapIds;

    /**
     * Creates a new ImageMinecraftManager.
     *
     * @param id The next id to use.
     * @param minecraftImages The list of ImageMinecraft.
     * @param unusedMapIds The list of unused MapIdComponent.
     */
    private MinecraftImageManager(int id, List<MinecraftImage> minecraftImages, List<MapIdComponent> unusedMapIds) {
        this.nextId = id;
        this.MinecraftImages = minecraftImages;
        this.unusedMapIds = unusedMapIds;
    }

    /**
     * Gets the instance of the ImageMinecraftManager.
     *
     * @return The instance of the ImageMinecraftManager.
     */
    public static MinecraftImageManager getInstance() {
        if (instance == null) {
            instance = deserialize();
        }
        return instance;
    }

    /**
     * Gets the next id to use and increments it.
     *
     * @return The next id to use.
     */
    public int getNextId() {
        return this.nextId++;
    }

    /**
     * Gets the list of unused MapIdComponent.
     *
     * @return The list of unused MapIdComponent.
     */
    public List<MinecraftImage> getImages() {
        return this.MinecraftImages;
    }

    /**
     * Gets an ImageMinecraft by its id.
     *
     * @param id The id of the ImageMinecraft to get.
     * @return The ImageMinecraft with the given id.
     */
    public MinecraftImage getMapImageById(int id) {
        for (MinecraftImage image : this.MinecraftImages) {
            if (image.getId() == id) {
                return image;
            }
        }
        return null;
    }

    /**
     * Adds an ImageMinecraft to the manager.
     *
     * @param minecraftImage The ImageMinecraft to add.
     */
    public void add(MinecraftImage minecraftImage) {
        int numberOfMaps = 0;
        for (MinecraftImage currentMinecraftImage : this.MinecraftImages) {
            if (currentMinecraftImage.getCreatorId().equals(minecraftImage.getCreatorId())) {
                numberOfMaps++;
                if (currentMinecraftImage.getImage().getName().equals(minecraftImage.getImage().getName())) {
                    throw new IllegalArgumentException("An image with the same name already exists");
                }
            }
        }

        if (numberOfMaps >= MAX_MAPS_COUNT_BY_USER) {
            throw new IllegalArgumentException("User has too many maps, limit is " + MAX_MAPS_COUNT_BY_USER + " with current config");
        }

        this.MinecraftImages.add(minecraftImage);
    }

    /**
     * Removes an ImageMinecraft from the manager.
     *
     * @param minecraftImage The ImageMinecraft to remove.
     */
    public void remove(MinecraftImage minecraftImage) {
        for (MapIdComponent[] mapIds : minecraftImage.getMinecraftMapIds()) {
            this.unusedMapIds.addAll(Arrays.asList(mapIds));
        }

        this.MinecraftImages.remove(minecraftImage);
    }

    /**
     * Serializes the ImageMinecraftManager to a file.
     *
     * @param filePath The path to the file to serialize the ImageMinecraftManager to.
     */
    public void serialize(String filePath) {
        File file = new File(filePath);
        File parentDir = file.getParentFile();

        if (parentDir != null && !parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                LOGGER.error("Failed to create directories for {}", filePath);
                return;
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(json.getBytes());
            LOGGER.info("Serialized {} images to {}", this.MinecraftImages.size(), filePath);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize ImageMinecraftManager", e);
        }
    }

    /**
     * Deserializes the ImageMinecraftManager from a file.
     *
     * @return The deserialized ImageMinecraftManager.
     */
    private static MinecraftImageManager deserialize() {
        File file = new File(MinecraftImageManager.STORAGE_PATH);
        if (!file.exists()) {
            return MinecraftImageManager.newInstance();
        }

        Gson gson = new Gson();

        try {
            MinecraftImageManager minecraftImageManager = gson.fromJson(new FileReader(file), MinecraftImageManager.class);
            if (minecraftImageManager != null) {
                return minecraftImageManager;
            } else {
                return MinecraftImageManager.newInstance();
            }
        } catch (Exception e) {
            return MinecraftImageManager.newInstance();
        }
    }

    /**
     * Creates a new ImageMinecraftManager with default values.
     *
     * @return The new ImageMinecraftManager.
     */
    private static MinecraftImageManager newInstance() {
        return new MinecraftImageManager(0, new ArrayList<>(), new ArrayList<>());
    }
}
