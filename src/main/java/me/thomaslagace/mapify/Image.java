package me.thomaslagace.mapify;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;
import javax.imageio.ImageIO;

/**
 * Represents an image.
 */
public class Image {
    private final int id;
    private final UUID creatorId;
    private String name;
    private final URL url;
    private final int maxWidth;
    private final int maxHeight;

    private static final String IMAGE_STORAGE_PATH = "mapify/images/";

    /**
     * Creates a new Image.
     *
     * @param id The id of the image.
     * @param creatorId The UUID of the creator of the image.
     * @param name The name of the image.
     * @param url The URL of the image.
     * @param maxWidth The maximum width of the image.
     * @param maxHeight The maximum height of the image.
     */
    public Image(int id, UUID creatorId, String name, URL url, int maxWidth, int maxHeight) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Name must not contain special characters");
        }
        this.id = id;
        this.creatorId = creatorId;
        this.name = name;
        this.url = url;
        this.maxWidth = maxWidth;
        this.maxHeight = maxHeight;
    }

    /**
     * Creates a new Image.
     *
     * @param id The id of the image.
     * @param creatorId The UUID of the creator of the image.
     * @param name The name of the image.
     * @param url The URL of the image.
     */
    public Image(int id, UUID creatorId, String name, URL url) {
        this(id, creatorId, name, url, Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public int getId() {
        return id;
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

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    /**
     * Returns if the name is valid.
     *
     * @param name The name to check.
     * @return If the name is valid.
     */
    public static boolean isValidName(String name) {
        return !name.matches(".*[*?!@#$%^&(){}\\[\\]:;<>,./~`_+=|\\-].*");
    }

    /**
     * Serialize the image.
     */
    public void serialize() throws IOException {
        storeImage(fetchImage());
    }

    /**
     * Fetches the image from the URL.
     *
     * @return The image. Resized if it is larger than the maximum width and height.
     * @throws IOException If the image could not be fetched.
     */
    public BufferedImage fetchImage() throws IOException {
        BufferedImage image = ImageIO.read(url);
        if (image.getHeight() > maxHeight || image.getWidth() > maxWidth) {
            image = ImageUtil.resizeKeepRatio(image, maxWidth, maxHeight);
        }
        return image;
    }

    /**
     * Stores the image on disk. The image will be resized to the width and height of the image to make sure it doesn't
     * store a huge image.
     *
     * @param image The image to store.
     */
    public void storeImage(BufferedImage image) throws IOException {
        File directory = new File(IMAGE_STORAGE_PATH + creatorId.toString());
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory");
        }

        File file = new File(directory, name + ".png");

        ImageIO.write(image, "png", file);
    }
}
