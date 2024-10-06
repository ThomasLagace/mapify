package me.thomaslagace.mapify.map;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

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

    private static final String IMAGE_STORAGE_PATH = "mapify/images/";

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

        Pattern pattern = Pattern.compile("[*?!@#$%^&(){}\\[\\]:;<>,./~`_+=|\\-]");
        if (pattern.matcher(name).find()) {
            throw new IllegalArgumentException("Name must not contain special characters");
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

    /**
     * Fetches the image from the URL.
     *
     * @return The image.
     */
    public BufferedImage fetchImage() {
        try {
            return ImageIO.read(url);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Stores the image on disk. The image will be resized to the width and height of the image to make sure it doesn't
     * store a huge image.
     *
     * @param image The image to store.
     */
    public void storeImage(BufferedImage image) {
        File directory = new File(IMAGE_STORAGE_PATH + creatorId.toString());
        if (!directory.exists() && !directory.mkdirs()) {
            return;
        }

        File file = new File(directory, name + ".png");

        BufferedImage resizedImage = resizeImage(image);

        try {
            ImageIO.write(resizedImage, "png", file);
        } catch (Exception e) {
            return;
        }
    }

    /**
     * Resizes the image to the width and height equivalent of map size.
     *
     * @param image The image to resize.
     * @return The resized image.
     */
    public BufferedImage resizeImage(BufferedImage image) {
        int newWidth = width * 128;
        int newHeight = height * 128;
        BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        resizedImage.getGraphics().drawImage(image, 0, 0, newWidth, newHeight, null);
        return resizedImage;
    }

    /**
     * Loads the image from disk.
     *
     * @return The image.
     */
    public BufferedImage loadImage() {
        File file = new File(IMAGE_STORAGE_PATH + creatorId.toString(), name + ".png");

        try {
            return ImageIO.read(file);
        } catch (Exception e) {
            return null;
        }
    }
}
