package me.thomaslagace.mapify.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class OnDiskImageGetter implements IImageGetter {
    private final File file;

    public OnDiskImageGetter(File file) {
        this.file = file;
    }

    public File getFile() {
        return this.file;
    }

    @Override
    public BufferedImage getImage() throws IOException {
        if (!this.file.exists()) {
            return null;
        }

        return ImageIO.read(this.file);
    }

    public void setImage(BufferedImage image) throws IOException {
        File parentDir = this.file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        ImageIO.write(image, "png", this.file);
    }
}
