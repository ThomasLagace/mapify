package me.thomaslagace.mapify;

import java.awt.image.BufferedImage;

/**
 * Utility class for image manipulation.
 */
public class ImageUtil {
    /**
     * Resizes an image to the specified width and height.
     *
     * @param image The image to resize.
     * @param width The width of the resized image.
     * @param height The height of the resized image.
     * @return The resized image.
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        resizedImage.getGraphics().drawImage(image.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH), 0, 0, null);

        return resizedImage;
    }

    /**
     * Resizes an image to the specified width and height and keeps the aspect ratio.
     *
     * @param image The image to resize.
     * @param width The width of the resized image.
     * @param height The height of the resized image.
     * @return The resized image.
     */
    public static BufferedImage resizeKeepRatio(BufferedImage image, int width, int height) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();

        double widthRatio = (double) width / originalWidth;
        double heightRatio = (double) height / originalHeight;

        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        return resizeImage(image, newWidth, newHeight);
    }
}
