package me.thomaslagace.mapify.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
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

    /**
     * Gets an image from a URL.
     *
     * @param stringUrl The URL of the image.
     * @param allowedDomains The allowed domains for the image.
     * @param maxSizeBytes The maximum size of the image in bytes.
     * @return The image from the URL.
     * @throws IOException If an I/O error occurs.
     * @throws URISyntaxException If the URL is not valid.
     */
    // TODO: Refactor this method.
    public static BufferedImage getImageFromURL(String stringUrl, List<String> allowedDomains, long maxSizeBytes) throws DomainNotAllowedException, InvalidImageException, IOException, URISyntaxException {
        URL url = (new URI(stringUrl)).toURL();

        URL[] allowedDomainsUrls = new URL[allowedDomains.length];

        for (int i = 0; i < allowedDomains.length; i++) {
            allowedDomainsUrls[i] = (new URI(allowedDomains.get(i))).toURL();
        }

        return ImageUtil.getImageFromURL(url, allowedDomainsUrls, maxSizeBytes);
    }

    /**
     * Gets an image from a URL.
     *
     * @param url The URL of the image.
     * @param allowedDomains The allowed domains for the image.
     * @param maxSizeBytes The maximum size of the image in bytes.
     * @return The image from the URL.
     * @throws IOException If an I/O error occurs.
     */
    public static BufferedImage getImageFromURL(URL url, URL[] allowedDomains, long maxSizeBytes) throws DomainNotAllowedException, InvalidImageException, IOException {
        boolean domainIsAllowed = Arrays.stream(allowedDomains).anyMatch(allowedDomain -> allowedDomain.getHost().equals(url.getHost()))
                                  || allowedDomains.length == 0;
        if (!domainIsAllowed) {
            throw new DomainNotAllowedException("Domain is not allowed");
        }

        if (!urlIsImage(url, maxSizeBytes)) {
            throw new InvalidImageException("The URL is not valid or the image is too large");
        }

        return ImageIO.read(url);
    }

    /**
     * Check if the URL has an image with a byte size smaller than the specified size.
     *
     * @param url The URL to check.
     * @param maxSizeBytes The maximum size of the image in bytes.
     * @return True if the URL is an image and is smaller than the specified size, false otherwise.
     * @throws IOException If an I/O error occurs.
     */
    public static boolean urlIsImage(URL url, long maxSizeBytes) throws IOException {
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestMethod("HEAD");
        httpConnection.connect();

        String contentType = httpConnection.getContentType();
        int contentLength = httpConnection.getContentLength();

        httpConnection.disconnect();

        return contentType.startsWith("image/")
               && contentLength > 0
               && contentLength <= maxSizeBytes;
    }
}
