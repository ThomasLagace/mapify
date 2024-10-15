package me.thomaslagace.mapify;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import me.thomaslagace.mapify.map.MinecraftColorMapUtil;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.map.MapState;
import net.minecraft.world.World;
import org.slf4j.Logger;

/**
 * Bridge between an image and the Minecraft maps that it is displayed on.
 */
public class ImageMinecraft {
    private final UUID creatorId;
    private final String name;
    private final Image image;
    private final MapIdComponent[][] minecraftMapIds;

    private static final Logger LOGGER = Mapify.LOGGER;

    /**
     * Creates a new ImageMinecraft.
     *
     * @param creatorId The UUID of the creator of the image.
     * @param name The name of the image.
     * @param image The image.
     * @param minecraftMapIds The ids of the Minecraft maps that the image is displayed on.
     */
    public ImageMinecraft(UUID creatorId, String name, Image image, MapIdComponent[][] minecraftMapIds) {
        this.creatorId = creatorId;
        this.name = name;
        this.image = image;
        this.minecraftMapIds = minecraftMapIds;
    }

    /**
     * Creates a new ImageMinecraft.
     *
     * @param creatorId The UUID of the creator of the image.
     * @param name The name of the image.
     * @param image The image.
     * @param width The width of the map.
     * @param height The height of the map.
     */
    public ImageMinecraft(UUID creatorId, String name, Image image, int width, int height) {
        this(creatorId, name, image, new MapIdComponent[height][width]);
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public String getName() {
        return name;
    }

    public MapIdComponent[][] getMinecraftMapIds() {
        return minecraftMapIds;
    }

    public Image getImage() {
        return image;
    }

    public int getHeight() {
        return minecraftMapIds.length;
    }

    public int getWidth() {
        return minecraftMapIds[0].length;
    }

    /**
     * Reserves map ids in the world.
     *
     * @param world The world to reserve map ids in.
     */
    public void reserveMapIds(World world) {
        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                minecraftMapIds[y][x] = world.increaseAndGetMapId();
            }
        }
    }

    /**
     * Assigns the map state of the maps in the world with the colors of the image.
     *
     * @param world The world to assign the map states to.
     */
    public void assignMapStateToWorld(World world) {
        BufferedImage bufferedImage;
        try {
            bufferedImage = image.getImage();
        } catch (IOException e) {
            LOGGER.error("Could not assign map state to world", e);
            return;
        }

        BufferedImage resizedImage = ImageUtil.resizeKeepRatio(bufferedImage, getWidth() * 128, getHeight() * 128);

        for (int y = 0; y < getHeight(); y++) {
            for (int x = 0; x < getWidth(); x++) {
                MapIdComponent mapId = minecraftMapIds[y][x];
                MapState mapState = MapState.of((byte) 0, true, world.getRegistryKey());

                for (int i = 0; i < 128; i++) {
                    for (int j = 0; j < 128; j++) {
                        int color = resizedImage.getRGB(x * 128 + i, y * 128 + j);

                        int alpha = (color >> 24) & 0xff;
                        int red = (color >> 16) & 0xff;
                        int green = (color >> 8) & 0xff;
                        int blue = color & 0xff;

                        mapState.setColor(i, j, MinecraftColorMapUtil.getClosestRenderColor(red, green, blue));
                    }
                }

                world.putMapState(mapId, mapState);
            }
        }
    }
}
