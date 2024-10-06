package me.thomaslagace.mapify.map;

import net.minecraft.block.MapColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class MinecraftColorMapUtil {
    private static final List<Color> allColors = new ArrayList<>();

    static {
        allColors.add(new Color(127, 178, 56));
        allColors.add(new Color(247, 233, 163));
        allColors.add(new Color(199, 199, 199));
        allColors.add(new Color(255, 0, 0));
        allColors.add(new Color(160, 160, 255));
        allColors.add(new Color(167, 167, 167));
        allColors.add(new Color(0, 124, 0));
        allColors.add(new Color(255, 255, 255));
        allColors.add(new Color(164, 168, 184));
        allColors.add(new Color(151, 109, 77));
        allColors.add(new Color(112, 112, 112));
        allColors.add(new Color(64, 64, 255));
        allColors.add(new Color(143, 119, 72));
        allColors.add(new Color(255, 252, 245));
        allColors.add(new Color(216, 127, 51));
        allColors.add(new Color(178, 76, 216));
        allColors.add(new Color(102, 153, 216));
        allColors.add(new Color(229, 229, 51));
        allColors.add(new Color(127, 204, 25));
        allColors.add(new Color(242, 127, 165));
        allColors.add(new Color(76, 76, 76));
        allColors.add(new Color(153, 153, 153));
        allColors.add(new Color(76, 127, 153));
        allColors.add(new Color(127, 63, 178));
        allColors.add(new Color(51, 76, 178));
        allColors.add(new Color(102, 76, 51));
        allColors.add(new Color(102, 127, 51));
        allColors.add(new Color(153, 51, 51));
        allColors.add(new Color(25, 25, 25));
        allColors.add(new Color(250, 238, 77));
        allColors.add(new Color(92, 219, 213));
        allColors.add(new Color(74, 128, 255));
        allColors.add(new Color(0, 217, 58));
        allColors.add(new Color(129, 86, 49));
        allColors.add(new Color(112, 2, 0));
        allColors.add(new Color(209, 177, 161));
        allColors.add(new Color(159, 82, 36));
        allColors.add(new Color(149, 87, 108));
        allColors.add(new Color(112, 108, 138));
        allColors.add(new Color(186, 133, 36));
        allColors.add(new Color(103, 117, 53));
        allColors.add(new Color(160, 77, 78));
        allColors.add(new Color(57, 41, 35));
        allColors.add(new Color(135, 107, 98));
        allColors.add(new Color(87, 92, 92));
        allColors.add(new Color(122, 73, 88));
        allColors.add(new Color(76, 62, 92));
        allColors.add(new Color(76, 50, 35));
        allColors.add(new Color(76, 82, 42));
        allColors.add(new Color(142, 60, 46));
        allColors.add(new Color(37, 22, 16));
        allColors.add(new Color(189, 48, 49));
        allColors.add(new Color(148, 63, 97));
        allColors.add(new Color(92, 25, 29));
        allColors.add(new Color(22, 126, 134));
        allColors.add(new Color(58, 142, 140));
        allColors.add(new Color(86, 44, 62));
        allColors.add(new Color(20, 180, 133));
        allColors.add(new Color(100, 100, 100));
        allColors.add(new Color(216, 175, 147));
        allColors.add(new Color(127, 167, 150));
    }

    public static byte getClosestRenderColor(int r, int g, int b) {
        byte closestColor = 0;
        double closestDistance = Double.MAX_VALUE;

        for (MapColor.Brightness brightness : MapColor.Brightness.values()) {
            double multiplier = brightness.brightness / 255.0;

            for (Color color : allColors) {
                double red = color.getRed() * multiplier;
                double green = color.getGreen() * multiplier;
                double blue = color.getBlue() * multiplier;

                double redDiff = Math.abs(red - r);
                double greenDiff = Math.abs(green - g);
                double blueDiff = Math.abs(blue - b);

                double distance = Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);

                if (distance < closestDistance) {
                    closestDistance = distance;
                    MapColor closestColorMapColor = MapColor.get(allColors.indexOf(color) + 1);
                    closestColor = closestColorMapColor.getRenderColorByte(brightness);
                }
            }
        }

        return closestColor;
    }
}
