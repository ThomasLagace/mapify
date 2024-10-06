package me.thomaslagace.mapify.map;

import com.google.gson.Gson;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MapsManager {
    private static final List<MapsImage> mapsImages = new ArrayList<>();

    public static void initialize(String mapsJson) {
        Gson gson = new Gson();

        mapsImages.addAll(List.of(gson.fromJson(mapsJson, MapsImage[].class)));
    }

    public static void add(MapsImage mapsImage) {
        mapsImages.add(mapsImage);
    }

    public static void remove(MapsImage mapsImage) {
        mapsImages.remove(mapsImage);
    }

    public static List<MapsImage> getMapsImages() {
        return mapsImages;
    }

    public static void serialize(String filePath) {
        try {
            serialize(new FileOutputStream(filePath));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void serialize(FileOutputStream fileOutputStream) {
        Gson gson = new Gson();

        String json = gson.toJson(mapsImages);

        try {
            fileOutputStream.write(json.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
