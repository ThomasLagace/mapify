package me.thomaslagace.mapify.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

public class UrlImageGetter implements IImageGetter {
    private final URL url;
    private final long maxSizeBytes;

    public UrlImageGetter(URL url, long maxSizeBytes) {
        this.url = url;
        this.maxSizeBytes = maxSizeBytes;
    }

    public URL getUrl() {
        return this.url;
    }

    @Override
    public BufferedImage getImage() throws IOException {
        return ImageUtil.getImageFromURL(this.url, this.maxSizeBytes);
    }
}
