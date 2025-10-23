package me.thomaslagace.mapify.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

    public void addAndReserveMinecraftImage(MinecraftImage minecraftImage, int width, int height) {
        for (MinecraftImage imageImage : this.getMinecraftImagesByCreatorId(minecraftImage.getCreatorId())) {
            if (imageImage.getName().equals(minecraftImage.getName())) {
                throw new IllegalArgumentException("There is already an image with the same name");
            }
        }

        List<MapIdComponent> mapIds = getAndReserveMapIds(width * height);

        MapIdComponent[][] formatedMapIds = new MapIdComponent[height][width];

        for (int i = 0; i < mapIds.size(); i++) {
            int y = i / width;
            int x = i % width;
        }
    }

    public List<MinecraftImage> getMinecraftImagesByCreatorId(UUID playerId) {
        List<MinecraftImage> minecraftImages = new ArrayList<>();

        for (MinecraftImage image : this.minecraftImages) {
            if (image.getCreatorId().equals(playerId)) {
                minecraftImages.add(image);
            }
        }
        return minecraftImages;
    }

    private List<MapIdComponent> getAndReserveMapIds(int amountToReserve) {
        List<MapIdComponent> mapIds = new ArrayList<>();
        for (MapIdComponent mapIdComponent : this.unusedMapIds) {
            if (mapIds.size() >= amountToReserve) {
                break;
            }
            mapIds.add(mapIdComponent);
            this.unusedMapIds.remove(mapIdComponent);
        }

        if (mapIds.size() < amountToReserve) {
            mapIds.addAll(reserveNewMapIds(amountToReserve - mapIds.size()));
        }

        return mapIds;
    }

    private List<MapIdComponent> reserveNewMapIds(int amountToReserve) {
        List<MapIdComponent> newMapIds = new ArrayList<MapIdComponent>();
        for (int i = 0; i < amountToReserve; i++) {
            newMapIds.add(world.increaseAndGetMapId());
        }

        return newMapIds;
    }

    public String serialize() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    private static MinecraftImagesDatabase deserialize(String json)  {
        Gson gson = new Gson();

        return gson.fromJson(json, MinecraftImagesDatabase.class);
    }
}
