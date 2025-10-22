package me.thomaslagace.mapify.minecraft;

import java.util.UUID;

import me.thomaslagace.mapify.image.OnDiskImageGetter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * Bridge between an image and the Minecraft maps that it is displayed on.
 */
public class MinecraftImage {
    private final String name;
    private final UUID creatorId;
    private final OnDiskImageGetter ondiskImageGetter;
    private MapIdComponent[][] minecraftMapIds;

    public MinecraftImage(String name, UUID creatorId, OnDiskImageGetter ondiskImageGetter) {
        this.name = name;
        this.creatorId = creatorId;
        this.ondiskImageGetter = ondiskImageGetter;
    }

    public MinecraftImage(String name, UUID creatorId, OnDiskImageGetter onDiskImageGetter, MapIdComponent[][] minecraftMapIds) {
        this(name, creatorId, onDiskImageGetter);
        this.minecraftMapIds = minecraftMapIds;
    }

    public void assignMapIds(MapIdComponent[][] minecraftMapIds) {
        this.minecraftMapIds = minecraftMapIds;
    }

    public String getName() {
        return name;
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
     * Returns the map item stacks of the maps.
     *
     * @return The map item stacks of the maps.
     */
    public ItemStack[][] getMapItemStack() {
        if (this.minecraftMapIds == null) {
            return null;
        }
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
