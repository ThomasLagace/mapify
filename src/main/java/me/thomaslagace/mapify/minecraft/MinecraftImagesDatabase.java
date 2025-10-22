package me.thomaslagace.mapify.minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.component.type.MapIdComponent;
import net.minecraft.world.World;

public class MinecraftImagesDatabase {
    private final List<MinecraftImage> minecraftImages;
    private final List<MapIdComponent> unusedMapIds;
    private final World world;

    public MinecraftImagesDatabase(List<MinecraftImage> minecraftImages, List<MapIdComponent> unusedMapIds, World world) {
        this.minecraftImages = minecraftImages;
        this.unusedMapIds = unusedMapIds;
        this.world = world;
    }

    public List<MinecraftImage> getMinecraftImages() {
        return Collections.unmodifiableList(minecraftImages);
    }

    private List<MapIdComponent> reserveNewMapIds(int amountToReserve) {
        List<MapIdComponent> newMapIds = new ArrayList<MapIdComponent>();
        for (int i = 0; i < amountToReserve; i++) {
            newMapIds.add(world.increaseAndGetMapId());
        }

        return newMapIds;
    }
}
