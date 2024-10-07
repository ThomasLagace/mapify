package me.thomaslagace.mapify.map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import me.thomaslagace.mapify.Mapify;

/**
 * Manages all the maps images.
 */
public class MapsManager {
    private static final List<MapImage> mapImages = new ArrayList<>();

    private static class MapsManagerData {
        public List<MapImage> mapImages;

        public MapsManagerData(List<MapImage> mapImages) {
            this.mapImages = mapImages;
        }
    }

    /**
     * Initializes the MapsManager by deserializing the maps images from a file.
     *
     * @param filePath The path to the file to deserialize the maps images from.
     */
    public static void initialize(String filePath) {
        deserialize(filePath);
    }

    /**
     * Adds a map image to the manager if an image with the same name and creator does not already exist.
     *
     * @param mapImage The maps image to add.
     */
    public static void add(MapImage mapImage) {
        for (MapImage existingMapImage : mapImages) {
            if (Objects.equals(existingMapImage.getName(), mapImage.getName())
                && Objects.equals(existingMapImage.getCreatorId(), mapImage.getCreatorId())) {
                throw new IllegalArgumentException("Image with the same name and creator already exists");
            }
        }

        mapImages.add(mapImage);
    }

    /**
     * Removes a maps image from the manager.
     *
     * @param mapImage The maps image to remove.
     * @return True if the maps image was removed, false otherwise.
     */
    public static boolean remove(MapImage mapImage) {
        return mapImages.remove(mapImage);
    }

    /**
     * Gets an unmodifiable list of all the maps images.
     *
     * @return An unmodifiable list of all the maps images.
     */
    public static List<MapImage> getMapsImages() {
        return Collections.unmodifiableList(mapImages);
    }

    /**
     * Deserializes the maps images from a file.
     *
     * @param filePath The path to the file to deserialize the maps images from.
     */
    public static void deserialize(String filePath) {
        File file = new File(filePath);
        deserialize(file);
    }

    /**
     * Deserializes the maps images from a file.
     *
     * @param file The file to deserialize the maps images from.
     */
    public static void deserialize(File file) {
        if (!file.exists()) {
            return;
        }

        Gson gson = new Gson();

        try {
            MapsManagerData mapsManagerData = gson.fromJson(new FileReader(file), MapsManagerData.class);
            if (mapsManagerData != null) {
                mapImages.addAll(mapsManagerData.mapImages);
            }
        } catch (Exception e) {
            Mapify.LOGGER.error("Failed to deserialize maps images", e);
        }
    }

    /**
     * Serializes the maps images to a file.
     *
     * @param filePath The path to the file to serialize the maps images to.
     */
    public static void serialize(String filePath) {
        File file = new File(filePath);
        serialize(file);
    }

    /**
     * Serializes the maps images to a file.
     *
     * @param file The file to serialize the maps images to.
     */
    public static void serialize(File file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String json = gson.toJson(new MapsManagerData(mapImages));

        Mapify.LOGGER.info("Serializing maps images to file: " + file.getAbsolutePath());

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(json.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            Mapify.LOGGER.error("Failed to serialize maps images", e);
        }
    }
}
