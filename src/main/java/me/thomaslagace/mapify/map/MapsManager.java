package me.thomaslagace.mapify.map;

import com.google.gson.Gson;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.thomaslagace.mapify.Mapify;

public class MapsManager {
    private static final List<MapsImage> mapsImages = new ArrayList<>();

    public static void initialize(String filePath) {
        deserialize(filePath);
    }

    public static void add(MapsImage mapsImage) {
        mapsImages.add(mapsImage);
    }

    public static boolean remove(MapsImage mapsImage) {
        return mapsImages.remove(mapsImage);
    }

    public static List<MapsImage> getMapsImages() {
        return Collections.unmodifiableList(mapsImages);
    }

    public static void deserialize(String filePath) {
        File file = new File(filePath);
        deserialize(file);
    }

    public static void deserialize(File file) {
        if (!file.exists()) {
            return;
        }

        Gson gson = new Gson();

        try {
            List<MapsImage> allMapsImages = List.of(gson.fromJson(new FileReader(file), MapsImage[].class));
            mapsImages.addAll(allMapsImages);
        } catch (Exception e) {
            Mapify.LOGGER.error("Failed to deserialize maps images", e);
        }
    }

    public static void serialize(String filePath) {
        File file = new File(filePath);
        serialize(file);
    }

    public static void serialize(File file) {
        Gson gson = new Gson();

        String json = gson.toJson(mapsImages);

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
