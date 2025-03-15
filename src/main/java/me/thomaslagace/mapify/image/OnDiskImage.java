package me.thomaslagace.mapify.image;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OnDiskImage extends TemporaryInMemoryImage {
    private final File file;

    public OnDiskImage(String name, File file) {
        super(name);
        this.file = file;
    }

    @Override
    public BufferedImage getImage() {
        if (!this.file.exists()) {
            return null;
        }

        try {
            return ImageIO.read(this.file);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setImage(BufferedImage image) throws IOException {
        ImageIO.write(image, "png", this.file);
    }
}
