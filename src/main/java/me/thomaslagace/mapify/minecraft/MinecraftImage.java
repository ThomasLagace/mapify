package me.thomaslagace.mapify.minecraft;

import java.util.ArrayList;
import java.util.UUID;
import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.image.OnDiskImageGetter;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Bridge between an image and the Minecraft maps that it is displayed on.
 */
public class MinecraftImage {
    private final UUID id;
    private final String name;
    private final UUID creatorId;
    private final OnDiskImageGetter ondiskImageGetter;
    private final MapIdComponent[][] minecraftMapIds;

    public MinecraftImage(UUID id, String name, UUID creatorId, OnDiskImageGetter ondiskImageGetter, MapIdComponent[][] minecraftMapIds) {
        this.id = id;
        this.name = name;
        this.creatorId = creatorId;
        this.ondiskImageGetter = ondiskImageGetter;
        this.minecraftMapIds = minecraftMapIds;
    }

    public UUID getId() {
        return id;
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

    public OnDiskImageGetter getOndiskImageGetter() {
        return this.ondiskImageGetter;
    }

    /**
     * Gets the ids of the Minecraft maps that the image is displayed on.
     *
     * @return The ids of the Minecraft maps that the image is displayed on.
     */
    public MapIdComponent[][] getMinecraftMapIds() {
        return this.minecraftMapIds;
    }

    public boolean assignMapsToWorld(World world) {
        try {
            MinecraftImageUtils.assignMapStateToWorld(this.ondiskImageGetter.getImage(), this.minecraftMapIds, world);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Returns the map item stacks of the maps.
     *
     * @return The map item stacks of the maps.
     */
    public ItemStack[][] getDisplayMaps() {
        if (this.minecraftMapIds == null) {
            return null;
        }
        int width = this.minecraftMapIds[0].length;
        int height = this.minecraftMapIds.length;

        ItemStack[][] maps = new ItemStack[height][width];

        for (int y = 0; y < width; y++) {
            for (int x = 0; x < height; x++) {
                MapIdComponent mapId = this.minecraftMapIds[y][x];
                ItemStack mapItem = new ItemStack(Items.FILLED_MAP, 1);
                mapItem.set(DataComponentTypes.MAP_ID, mapId);
                maps[y][x] = mapItem;
            }
        }

        return maps;
    }

    public ItemStack createDataMapItem() {
        if (this.minecraftMapIds == null) {
            return null;
        }
        ItemStack paper = new ItemStack(Items.PAPER, 1);

        int width = this.minecraftMapIds[0].length;
        int height = this.minecraftMapIds.length;

        Text paperName = Text.of(String.format("%s %s x %s", this.name, width, height));
        paper.set(DataComponentTypes.ITEM_NAME, paperName);

        String creatorName = this.creatorId.toString();

        LoreComponent loreComponent = new LoreComponent(new ArrayList<>());
        loreComponent.lines().add(Text.of(this.name));
        loreComponent.lines().add(Text.of(Config.CREATED_BY + creatorName));
        paper.set(DataComponentTypes.LORE, loreComponent);

        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putBoolean("mapify", true);
        nbtCompound.putUuid("mapify.id", this.id);

        NbtComponent nbt = NbtComponent.of(nbtCompound);
        paper.set(DataComponentTypes.CUSTOM_DATA, nbt);

        return paper;
    }

    public static class Builder {
        private UUID id;
        private String name;
        private UUID creatorId;
        private OnDiskImageGetter ondiskImageGetter;
        private MapIdComponent[][] minecraftMapIds;

        public Builder setId(UUID id) {
            this.id = id;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCreatorId(UUID creatorId) {
            this.creatorId = creatorId;
            return this;
        }

        public Builder setOnDiskImageGetter(OnDiskImageGetter ondiskImageGetter) {
            this.ondiskImageGetter = ondiskImageGetter;
            return this;
        }

        public Builder setMinecraftMapIds(MapIdComponent[][] minecraftMapIds) {
            this.minecraftMapIds = minecraftMapIds;
            return this;
        }

        public MinecraftImage build() {
            MinecraftImage minecraftImage = new MinecraftImage(id, name, creatorId, ondiskImageGetter, minecraftMapIds);
            reset();
            return minecraftImage;
        }

        private void reset() {
            this.id = null;
            this.name = null;
            this.creatorId = null;
            this.ondiskImageGetter = null;
            this.minecraftMapIds = null;
        }
    }
}
