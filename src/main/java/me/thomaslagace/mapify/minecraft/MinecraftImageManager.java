package me.thomaslagace.mapify.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import me.thomaslagace.mapify.config.Config;
import net.minecraft.component.type.MapIdComponent;

/**
 * Manages all the Minecraft images in the application.
 */
public final class MinecraftImageManager {
    /**
     * The storage path for the ImageMinecraftManager.
     */
    private static final String STORAGE_PATH = Config.STORAGE_PATH;
    /**
     * The instance of the ImageMinecraftManager.
     */
    private static MinecraftImageManager instance;
    /**
     * The next id to use and the list of ImageMinecraft.
     */
    private int nextId;
    /**
     * List of ImageMinecraft.
     */
    private final List<MinecraftImage> minecraftImages;
    /**
     * The list of unused MapIdComponent.
     */
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
        this.minecraftImages = minecraftImages;
        this.unusedMapIds = unusedMapIds;
    }

    /**
     * Gets the instance of the ImageMinecraftManager.
     *
     * @return The instance of the ImageMinecraftManager.
     */
    public static MinecraftImageManager getInstance() {
        if (instance == null) {
            try {
                instance = deserialize(STORAGE_PATH);
            } catch (FileNotFoundException e) {
                instance = newInstance();
            }
        }
        return instance;
    }

    /**
     * Gets the next id to use and increments.
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
        return this.minecraftImages;
    }

    /**
     * Gets the list of unused MapIdComponent.
     *
     * @return The list of unused MapIdComponent.
     */
    public List<MapIdComponent> getUnusedMapIds() {
        return this.unusedMapIds;
    }

    /**
     * Gets an ImageMinecraft by its id.
     *
     * @param id The id of the ImageMinecraft to get.
     * @return The ImageMinecraft with the given id.
     */
    public MinecraftImage getMapImageById(int id) {
        for (MinecraftImage image : this.minecraftImages) {
            if (image.getId() == id) {
                return image;
            }
        }
        return null;
    }

    /**
     * Gets a list of ImageMinecraft by the creator's UUID.
     *
     * @param playerId The UUID of the creator.
     * @return The list of ImageMinecraft with the given creator's UUID.
     */
    public List<MinecraftImage> getImagesByPlayerId(UUID playerId) {
        List<MinecraftImage> images = new ArrayList<>();
        for (MinecraftImage image : this.minecraftImages) {
            if (image.getCreatorId().equals(playerId)) {
                images.add(image);
            }
        }
        return images;
    }

    /**
     * Adds an ImageMinecraft to the manager.
     *
     * @param minecraftImage The ImageMinecraft to add.
     */
    public void add(MinecraftImage minecraftImage) {
        for (MinecraftImage image : this.getImagesByPlayerId(minecraftImage.getCreatorId())) {
            if (image.getImage().getName().equals(minecraftImage.getImage().getName())) {
                throw new IllegalArgumentException("There is already an image with the same name");
            }
        }

        this.minecraftImages.add(minecraftImage);
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

        minecraftImage.deleteImage();
        this.minecraftImages.remove(minecraftImage);
    }

    /**
     * Serializes the ImageMinecraftManager to a file.
     *
     * @param filePath The path to the file to serialize the ImageMinecraftManager to.
     */
    public void serialize(String filePath) throws IOException {
        File file = new File(filePath + "mapify.json");
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                throw new IOException("Failed to create the directory for serialisation: " + file.getParentFile());
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(this);

        FileOutputStream outputStream = new FileOutputStream(file);

        outputStream.write(json.getBytes());

        outputStream.close();
    }

    /**
     * Deserializes the ImageMinecraftManager from a file.
     *
     * @return The deserialized ImageMinecraftManager.
     */
    private static MinecraftImageManager deserialize(String filePath) throws FileNotFoundException {
        File file = new File(filePath);
        if (!file.exists()) {
            return MinecraftImageManager.newInstance();
        }

        Gson gson = new Gson();

        MinecraftImageManager minecraftImageManager = gson.fromJson(new FileReader(file), MinecraftImageManager.class);
        if (minecraftImageManager != null) {
            return minecraftImageManager;
        } else {
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
