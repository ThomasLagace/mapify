package me.thomaslagace.mapify.map;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

public class MapsImage {
    private final UUID creatorId;
    private final String name;
    private final URL url;
    private final int width;
    private final int height;
    private final int[] minecraftMapIds;

    public MapsImage(UUID creatorId, String name, URL url, int width, int height, int[] minecraftMapIds) throws IllegalArgumentException {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than 0");
        }

        if (minecraftMapIds.length != width * height) {
            throw new IllegalArgumentException("Ids array must have a length equal to width * height");
        }

        this.creatorId = creatorId;
        this.name = name;
        this.url = url;
        this.width = width;
        this.height = height;
        this.minecraftMapIds = minecraftMapIds;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int[] getMinecraftMapIds() {
        return minecraftMapIds;
    }
}
