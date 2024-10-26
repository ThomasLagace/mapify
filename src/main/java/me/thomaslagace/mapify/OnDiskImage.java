package me.thomaslagace.mapify;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Represents an image stored on disk.
 */
public class OnDiskImage {
    /**
     * The name of the image.
     */
    private final String name;
    /**
     * The folder path to the image.
     */
    private final Path absoluteDirectory;

    /**
     * Creates a new OnDiskImage.
     *
     * @param name The name of the image.
     * @param path The path to the image.
     * @param image The image to store.
     */
    public OnDiskImage(String name, Path path, BufferedImage image) {
        if (!OnDiskImage.nameIsValid(name)) {
            throw new IllegalArgumentException("Invalid name");
        }

        this.name = name;
        this.absoluteDirectory = path.toAbsolutePath();
    }

    /**
     * Gets the name of the image.
     *
     * @return The name of the image.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Gets the image extension.
     *
     * @return The image extension.
     */
    public String getImageExtension() {
        return "png";
    }

    /**
     * Gets the full image path.
     *
     * @return The full image path.
     */
    public Path getFullImagePath() {
        return Paths.get(this.absoluteDirectory + this.name + this.getImageExtension());
    }

    /**
     * Gets the file image.
     *
     * @return The file image.
     */
    public File getFileImage() {
        return this.getFullImagePath().toFile();
    }

    /**
     * Stores the image on disk.
     *
     * @param image The image to store.
     * @throws IOException If an I/O error occurs.
     */
    public void storeImageOnDisk(BufferedImage image) throws IOException {
        ImageIO.write(image, this.getImageExtension(), this.getFileImage());
    }

    /**
     * Loads the image from disk.
     *
     * @return The image loaded from disk.
     * @throws IOException If an I/O error occurs.
     */
    public BufferedImage loadImageFromDisk() throws IOException {
        return ImageIO.read(this.getFileImage());
    }

    /**
     * Checks if the name is valid.
     *
     * @param name The name to check.
     * @return True if the name is valid, false otherwise.
     */
    public static boolean nameIsValid(String name) {
        String regex = "[a-zA-Z0-9_]+";
        return !Pattern.compile(regex).matcher(name).find();
    }
}
