package me.thomaslagace.mapify.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class UrlImage extends TemporaryInMemoryImage {
    private final URL url;

    public UrlImage(String name, URL url) {
        super(name);
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
