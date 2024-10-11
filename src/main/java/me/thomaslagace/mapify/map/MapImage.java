package me.thomaslagace.mapify.map;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.map.MapState;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;
import java.util.Collections;
import java.util.UUID;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

/**
 * Represents an image that can be displayed on maps in Minecraft.
 */
public class MapImage {
    private final int id;
    private final UUID creatorId;
    private final String name;
    private final URL url;
    private final int width;
    private final int height;
    private final MapIdComponent[][] minecraftMapIds;

    private static final String IMAGE_STORAGE_PATH = "mapify/images/";

    /**
     * Creates a new MapsImage.
     *
     * @param creatorId The UUID of the creator of the image.
     * @param name The name of the image.
     * @param url The URL of the image.
     * @param width The width of the image.
     * @param height The height of the image.
     * @param minecraftMapIds The ids of the Minecraft maps that the image is displayed on.
     * @throws IllegalArgumentException If the width or height is less than or equal to 0
     * @throws IllegalArgumentException If the length of the ids array is not equal to width * height.
     */
    public MapImage(int id, UUID creatorId, String name, URL url, int width, int height, MapIdComponent[][] minecraftMapIds)
        throws IllegalArgumentException {

        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than 0");
        }

        if (minecraftMapIds.length != width
            || minecraftMapIds[0].length != height) {
            throw new IllegalArgumentException("Ids array must have a length equal to width * height");
        }

        Pattern pattern = Pattern.compile("[*?!@#$%^&(){}\\[\\]:;<>,./~`_+=|\\-]");
        if (pattern.matcher(name).find()) {
            throw new IllegalArgumentException("Name must not contain special characters");
        }

        this.id = id;
        this.creatorId = creatorId;
        this.name = name;
        this.url = url;
        this.width = width;
        this.height = height;
        this.minecraftMapIds = minecraftMapIds;
    }

    public int getId() {
        return id;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getName() {
        return name;
    }

    public URL getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public MapIdComponent[][] getMinecraftMapIds() {
        return minecraftMapIds;
    }


    /**
     * Assigns the map state of the maps in the world with the colors of the image.
     *
     * @param world The world to assign the map states to.
     */
    public void assignMapStateWithImage(World world) {
        BufferedImage image = loadImage();
        if (image == null) {
            return;
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                MapIdComponent mapId = minecraftMapIds[i][j];
                MapState mapState = MapState.of((byte) 0, true, world.getRegistryKey());

                for (int x = 0; x < 128; x++) {
                    for (int y = 0; y < 128; y++) {
                        int color = image.getRGB(i * 128 + x, j * 128 + y);

                        int red = (color >> 16) & 0xFF;
                        int green = (color >> 8) & 0xFF;
                        int blue = color & 0xFF;

                        mapState.setColor(x, y, MinecraftColorMapUtil.getClosestRenderColor(red, green, blue));
                    }
                }

                world.putMapState(mapId, mapState);
            }
        }
    }

    /**
     * Returns an array of itemStacks that represent the maps.
     *
     * @return An array of itemStacks that represent the maps.
     */
    public ItemStack[][] getMapsAsItemStacks() {
        ItemStack[][] itemStacks = new ItemStack[width][height];

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                MapIdComponent mapId = minecraftMapIds[i][j];

                ItemStack itemStack = new ItemStack(net.minecraft.item.Items.FILLED_MAP);
                itemStack.set(DataComponentTypes.MAP_ID, mapId);

                itemStacks[i][j] = itemStack;
            }
        }

        return itemStacks;
    }
}
