package me.thomaslagace.mapify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraft.component.type.MapIdComponent;
import org.slf4j.Logger;

/**
 * Manages all the Minecraft images in the application.
 */
public class ImageMinecraftManager {
    private static final Logger LOGGER = Mapify.LOGGER;
    private static final int MAX_MAPS_COUNT_BY_USER = Config.MAX_MAPS_COUNT_BY_USER;

    private int nextId = 0;
    private final List<ImageMinecraft> imageMinecrafts;
    private final List<MapIdComponent> unusedMapIds;

    /**
     * Creates a new ImageMinecraftManager.
     *
     * @param minecraftImages The Minecraft images.
     * @param unusedMapIds The unused map ids.
     */
    public ImageMinecraftManager(int id, List<ImageMinecraft> minecraftImages, List<MapIdComponent> unusedMapIds) {
        this.nextId = id;
        this.imageMinecrafts = minecraftImages;
        this.unusedMapIds = unusedMapIds;
    }

    public int getNextId() {
        return nextId++;
    }

    public List<ImageMinecraft> getImages() {
        return imageMinecrafts;
    }

    /**
     * Gets an ImageMinecraft by its id.
     *
     * @param id The id of the ImageMinecraft to get.
     * @return The ImageMinecraft with the given id.
     */
    public ImageMinecraft getMapImageById(int id) {
        for (ImageMinecraft image : imageMinecrafts) {
            if (image.getId() == id) {
                return image;
            }
        }
        return null;
    }

    /**
     * Adds an ImageMinecraft to the manager.
     *
     * @param imageMinecraft The ImageMinecraft to add.
     */
    public void add(ImageMinecraft imageMinecraft) {
        int numberOfMaps = 0;
        for (ImageMinecraft image : imageMinecrafts) {
            if (image.getCreatorId().equals(imageMinecraft.getCreatorId())) {
                numberOfMaps++;
                if (image.getName().equals(imageMinecraft.getName())) {
                    throw new IllegalArgumentException("An image with the same name already exists");
                }
            }
        }

        if (numberOfMaps >= MAX_MAPS_COUNT_BY_USER) {
            throw new IllegalArgumentException("User has too many maps, limit is " + MAX_MAPS_COUNT_BY_USER + " with current config");
        }

        imageMinecrafts.add(imageMinecraft);
    }

    /**
     * Removes an ImageMinecraft from the manager.
     *
     * @param imageMinecraft The ImageMinecraft to remove.
     */
    public void remove(ImageMinecraft imageMinecraft) {
        for (MapIdComponent[] mapIds : imageMinecraft.getMinecraftMapIds()) {
            unusedMapIds.addAll(Arrays.asList(mapIds));
        }
        imageMinecrafts.remove(imageMinecraft);
        imageMinecraft.delete();
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
            LOGGER.info("Serialized {} images to {}", imageMinecrafts.size(), filePath);
        } catch (Exception e) {
            LOGGER.error("Failed to serialize ImageMinecraftManager", e);
        }
    }

    /**
     * Deserializes the ImageMinecraftManager from a file.
     *
     * @param filePath The path to the file to deserialize the ImageMinecraftManager from.
     * @return The deserialized ImageMinecraftManager.
     */
    public static ImageMinecraftManager deserialize(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return new ImageMinecraftManager(0, new ArrayList<>(), new ArrayList<>());
        }

        Gson gson = new Gson();

        try {
            ImageMinecraftManager imageMinecraftManager = gson.fromJson(new FileReader(file), ImageMinecraftManager.class);
            if (imageMinecraftManager != null) {
                return imageMinecraftManager;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to deserialize ImageMinecraftManager", e);
        }
        return new ImageMinecraftManager(0, new ArrayList<>(), new ArrayList<>());
    }
}
