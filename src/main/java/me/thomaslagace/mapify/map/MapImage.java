package me.thomaslagace.mapify.map;

import java.net.URL;
import java.util.UUID;

/**
 * Represents an image that can be displayed on maps in Minecraft.
 */
public class MapImage {
    private final UUID creatorId;
    private final String name;
    private final URL url;
    private final int width;
    private final int height;
    private final int[] minecraftMapIds;

    /**
     * Creates a new MapsImage.
     *
     * @param creatorId The UUID of the creator of the image.
     * @param name The name of the image.
     * @param url The URL of the image.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param minecraftMapIds The ids of the Minecraft maps that the image is displayed on.
     * @throws IllegalArgumentException If the width or height is less than or equal to 0
     * @throws IllegalArgumentException If the length of the ids array is not equal to width * height.
     */
    public MapImage(UUID creatorId, String name, URL url, int width, int height, int[] minecraftMapIds)
        throws IllegalArgumentException {

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
