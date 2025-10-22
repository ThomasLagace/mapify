package me.thomaslagace.mapify.minecraft;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.UUID;
import me.thomaslagace.mapify.image.ImageUtil;
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
    private final UUID creatorId;
    private final MapIdComponent[][] minecraftMapIds;

    public MinecraftImage(UUID creatorId, MapIdComponent[][] minecraftMapIds) {
        this.creatorId = creatorId;
        this.minecraftMapIds = minecraftMapIds;
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
