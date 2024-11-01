package me.thomaslagace.mapify.minecraft;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import me.thomaslagace.mapify.image.ImageUtil;
import me.thomaslagace.mapify.image.OnDiskImage;
import net.minecraft.block.MapColor;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.world.World;

/**
 * Bridge between an image and the Minecraft maps that it is displayed on.
 */
public class MinecraftImage {
    /**
     * The id of the image minecraft.
     */
    private final int id;
    /**
     * The UUID of the creator of the image.
     */
    private final UUID creatorId;
    /**
     * The image.
     */
    private final OnDiskImage image;
    /**
     * The ids of the Minecraft maps that the image is displayed on.
     */
    private final MapIdComponent[][] minecraftMapIds;

    /**
     * Creates a new MinecraftImage.
     *
     * @param id The id of the image minecraft.
     * @param creatorId The UUID of the creator of the image.
     * @param image The image.
     * @param minecraftMapIds The ids of the Minecraft maps that the image is displayed on.
     */
    public MinecraftImage(int id, UUID creatorId, OnDiskImage image, MapIdComponent[][] minecraftMapIds) {
        this.id = id;
        this.creatorId = creatorId;
        this.image = image;
        this.minecraftMapIds = minecraftMapIds;
    }

    /**
     * Gets the id of the minecraft image.
     *
     * @return The id of the minecraft image.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the UUID of the creator of the image.
     *
     * @return The UUID of the creator of the image.
     */
    public UUID getCreatorId() {
        return this.creatorId;
    }

    /**
     * Gets the image.
     *
     * @return The image.
     */
    public OnDiskImage getImage() {
        return this.image;
    }

    /**
     * Gets the ids of the Minecraft maps that the image is displayed on.
     *
     * @return The ids of the Minecraft maps that the image is displayed on.
     */
    public MapIdComponent[][] getMinecraftMapIds() {
        return this.minecraftMapIds;
    }

    /**
     * Gets the height of the minecraft map image.
     *
     * @return The height of the minecraft map image.
     */
    public int getHeight() {
        return this.minecraftMapIds.length;
    }

    /**
     * Gets the width of the minecraft map image.
     *
     * @return The width of the minecraft map image.
     */
    public int getWidth() {
        return this.minecraftMapIds[0].length;
    }

    /**
     * Reserves map ids in the world.
     *
     * @param world The world to reserve map ids in.
     * @param unusedMapIds The list of unused MapIdComponent.
     */
    // TODO: Refactor this method to not use unusedMapIds directly.
    public void reserveMapIds(World world, List<MapIdComponent> unusedMapIds) {
        for (int y = 0; y < this.getHeight(); y++) {
            for (int x = 0; x < this.getWidth(); x++) {
                if (!unusedMapIds.isEmpty()) {
                    this.minecraftMapIds[y][x] = unusedMapIds.removeFirst();
                    continue;
                }
                this.minecraftMapIds[y][x] = world.increaseAndGetMapId();
            }
        }
    }

    /**
     * Assigns the map state of the maps in the world with the colors of the image.
     *
     * @param world The world to assign the map states to.
     */
    public void assignMapStateToWorld(World world) throws IOException {
        BufferedImage bufferedImage = this.image.loadImageFromDisk();

        BufferedImage resizedImage = ImageUtil.resizeKeepRatio(bufferedImage, this.getWidth() * 128, this.getHeight() * 128);

        for (int y = 0; y < this.getHeight(); y++) {
            for (int x = 0; x < this.getWidth(); x++) {
                MapIdComponent mapId = this.minecraftMapIds[y][x];
                MapState mapState = MapState.of((byte) 0, true, world.getRegistryKey());

                for (int i = 0; i < 128; i++) {
                    for (int j = 0; j < 128; j++) {
                        int color;
                        try {
                            color = resizedImage.getRGB(x * 128 + i, y * 128 + j);
                        } catch (ArrayIndexOutOfBoundsException e) {
                            mapState.setColor(i, j, MapColor.CLEAR.getRenderColorByte(MapColor.Brightness.NORMAL));
                            continue;
                        }

                        int alpha = (color >> 24) & 0xff;
                        int red = (color >> 16) & 0xff;
                        int green = (color >> 8) & 0xff;
                        int blue = color & 0xff;

                        if (alpha <= 128) {
                            mapState.setColor(i, j, MapColor.CLEAR.getRenderColorByte(MapColor.Brightness.NORMAL));
                        } else {
                            mapState.setColor(i, j, MinecraftColorMapUtil.getClosestRenderColor(red, green, blue));
                        }
                    }
                }

                world.putMapState(mapId, mapState);
            }
        }
    }

    /**
     * Returns the map item stacks of the maps.
     *
     * @return The map item stacks of the maps.
     */
    public ItemStack[][] getMapItemStack() {
        ItemStack[][] maps = new ItemStack[this.getHeight()][this.getWidth()];

        for (int y = 0; y < this.getHeight(); y++) {
            for (int x = 0; x < this.getWidth(); x++) {
                MapIdComponent mapId = this.minecraftMapIds[y][x];
                ItemStack mapItem = new ItemStack(Items.FILLED_MAP, 1);
                mapItem.set(DataComponentTypes.MAP_ID, mapId);
                maps[y][x] = mapItem;
            }
        }

        return maps;
    }
}
