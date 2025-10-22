package me.thomaslagace.mapify.image;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class UrlImage implements IImageGetter {
    private final URL url;

    public UrlImage(URL url) {
        this.url = url;
    }

    public URL getUrl() {
        return this.url;
    }

    @Override
    public BufferedImage getImage() throws IOException {
        return ImageIO.read(this.url);
    }
}
