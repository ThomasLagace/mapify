package me.thomaslagace.mapify.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

/**
 * Utility class for image manipulation.
 */
public final class ImageUtil {
    private ImageUtil() {
        throw new IllegalStateException("Utility class");
    }

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

    public static BufferedImage getImageFromURL(URL url, long maxSizeBytes) throws IOException {
        if (!isUrlAnImage(url)) {
            throw new IllegalArgumentException("URL is not an image");
        }

        if (isOnlineImageTooBig(url, maxSizeBytes)) {
            throw new IOException("Image is too big");
        }

        return getImageFromURL(url);
    }

    public static BufferedImage getImageFromURL(URL url) throws IOException {
        return ImageIO.read(url);
    }

    public static boolean isUrlAnImage(URL url) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setInstanceFollowRedirects(true);
        httpConnection.setRequestMethod("HEAD");
        httpConnection.connect();

        String contentType = httpConnection.getContentType();

        httpConnection.disconnect();

        return contentType != null && contentType.startsWith("image/");
    }

    public static boolean isOnlineImageTooBig(URL url, long maxSizeBytes) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setInstanceFollowRedirects(true);
        httpConnection.setRequestMethod("HEAD");
        httpConnection.connect();

        long contentLenght = httpConnection.getContentLengthLong();

        httpConnection.disconnect();

        return contentLenght < 0 || contentLenght > maxSizeBytes;
    }
}
