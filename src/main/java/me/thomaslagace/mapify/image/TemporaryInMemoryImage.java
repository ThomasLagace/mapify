package me.thomaslagace.mapify.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

public abstract class TemporaryInMemoryImage {
    private String name;

    public TemporaryInMemoryImage(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract BufferedImage getImage();
    public abstract void setImage(BufferedImage image) throws IOException;
}
