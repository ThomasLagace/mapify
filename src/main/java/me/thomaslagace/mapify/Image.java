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
    private static final int maxWidth = Config.MAX_IMAGE_WIDTH;
    private static final int maxHeight = Config.MAX_IMAGE_HEIGHT;
    private static final String imageStoragePath = Config.IMAGE_STORAGE_PATH;

    private UUID creatorId;
    private String name;
    private final URL url;

    /**
     * Creates a new Image.
     *
     * @param creatorId The UUID of the creator of the image.
     * @param name The name of the image.
     * @param url The URL of the image.
     */
    public Image(UUID creatorId, String name, URL url) {
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Name must not contain special characters");
        }
        this.creatorId = creatorId;
        this.name = name;
        this.url = url;
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
     * Returns the image. If the image is not stored on disk, it will be fetched from the URL and stored.
     *
     * @return The image.
     * @throws IOException If the image could not be fetched or serialized.
     */
    public BufferedImage getImage() throws IOException {
        try {
            return deserialize();
        } catch (IOException e) {
            BufferedImage image = fetchImage();
            storeImage(image);
            return image;
        }
    }

    /**
     * Serialize the image.
     */
    public void serialize() throws IOException {
        storeImage(fetchImage());
    }

    /**
     * Deserialize the image from disk.
     *
     * @return The image.
     */
    public BufferedImage deserialize() throws IOException {
        File file = new File(imageStoragePath + creatorId.toString() + "/" + name + ".png");
        return ImageIO.read(file);
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
        File directory = new File(imageStoragePath + creatorId.toString());
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Could not create directory");
        }

        File file = new File(directory, name + ".png");

        ImageIO.write(image, "png", file);
    }

    /**
     * Returns if the name is valid.
     *
     * @param name The name to check.
     * @return If the name is valid.
     */
    public static boolean isValidName(String name) {
        return !name.matches("[*?!@#$%^&(){}\\[\\]:;<>,./~`_+=|\\-]");
    }
}
