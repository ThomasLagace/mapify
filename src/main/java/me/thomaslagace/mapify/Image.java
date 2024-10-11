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
    private int maxWidth;
    private int maxHeight;

    private static final String IMAGE_STORAGE_PATH = "mapify/images/";

    /**
     * Creates a new Image.
     *
     * @param id The id of the image.
     * @param creatorId The UUID of the creator of the image.
     * @param name The name of the image.
     * @param url The URL of the image.
     */
    public Image(int id, UUID creatorId, String name, URL url) {
        this.id = id;
        this.creatorId = creatorId;
        this.name = name;
        this.url = url;
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

    /**
     * Serialize the image.
     */
    public void serialize() throws IOException {
        storeImage(fetchImage());
    }

    /**
     * Fetches the image from the URL.
     *
     * @return The image.
     * @throws IOException If the image could not be fetched.
     */
    public BufferedImage fetchImage() throws IOException {
        return ImageIO.read(url);
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
