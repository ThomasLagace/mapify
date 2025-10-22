package me.thomaslagace.mapify.image;

import java.awt.image.BufferedImage;
import java.io.IOException;

public interface IImageGetter {
    public BufferedImage getImage() throws IOException;
}
