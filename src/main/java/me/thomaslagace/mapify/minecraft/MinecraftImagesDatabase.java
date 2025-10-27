package me.thomaslagace.mapify.minecraft;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.*;

import net.minecraft.component.type.MapIdComponent;
import net.minecraft.world.World;

public class MinecraftImagesDatabase {
    private final List<MinecraftImage> minecraftImages;
    private final List<MapIdComponent> unusedMapIds;

    public MinecraftImagesDatabase(List<MinecraftImage> minecraftImages, List<MapIdComponent> unusedMapIds) {
        this.minecraftImages = minecraftImages;
        this.unusedMapIds = unusedMapIds;
    }

    public List<MinecraftImage> getMinecraftImages() {
        return Collections.unmodifiableList(minecraftImages);
    }

    public MinecraftImage getMinecraftImageById(UUID id) {
        for (MinecraftImage minecraftImage : minecraftImages) {
            if (minecraftImage.getId().equals(id)) {
                return minecraftImage;
            }
        }

        return null;
    }

    public MinecraftImage getMinecraftImageByNameAndCreatorId(String name, UUID creatorId) {
        for (MinecraftImage minecraftImage : getMinecraftImagesByCreatorId(creatorId)) {
            if (minecraftImage.getName().equals(name)) {
                return minecraftImage;
            }
        }

        return null;
    }

    public boolean userHasAlreadyThisImageName(UUID creatorId, String imageName) {
        for (MinecraftImage imageImage : this.getMinecraftImagesByCreatorId(creatorId)) {
            if (imageImage.getName().equals(imageName)) {
                return true;
            }
        }

         return false;
    }

    public boolean addAndReserveMinecraftImage(MinecraftImage.Builder imageBuilder, int width, int height, World world) {
        if (userHasAlreadyThisImageName(imageBuilder.getCreatorId(), minecraftImage.getName())) {
            return false;
        }

        List<MapIdComponent> mapIds = getAndReserveMapIds(width * height, world);

        MapIdComponent[][] formatedMapIds = new MapIdComponent[height][width];

        for (int i = 0; i < mapIds.size(); i++) {
            int y = i / width;
            int x = i % width;

            formatedMapIds[y][x] = mapIds.get(i);
        }

        minecraftImage.assignMapIds(formatedMapIds);
        minecraftImages.add(minecraftImage);
        return true;
    }

    public void removeMinecraftImage(MinecraftImage minecraftImage) {
        if (minecraftImages.remove(minecraftImage)) {
            for (MapIdComponent[] mapIdsComponents : minecraftImage.getMinecraftMapIds()) {
                unusedMapIds.addAll(Arrays.asList(mapIdsComponents));
            }
            minecraftImage.assignMapIds(null);
        }
    }

    public List<MinecraftImage> getMinecraftImagesByCreatorId(UUID creatorId) {
        List<MinecraftImage> minecraftImages = new ArrayList<>();

        for (MinecraftImage image : this.minecraftImages) {
            if (image.getCreatorId().equals(creatorId)) {
                minecraftImages.add(image);
            }
        }
        return minecraftImages;
    }

    private List<MapIdComponent> getAndReserveMapIds(int amountToReserve, World world) {
        List<MapIdComponent> mapIds = new ArrayList<>();
        for (MapIdComponent mapIdComponent : this.unusedMapIds) {
            if (mapIds.size() >= amountToReserve) {
                break;
            }
            mapIds.add(mapIdComponent);
            this.unusedMapIds.remove(mapIdComponent);
        }

        if (mapIds.size() < amountToReserve) {
            mapIds.addAll(reserveNewMapIds(amountToReserve - mapIds.size(), world));
        }

        return mapIds;
    }

    private List<MapIdComponent> reserveNewMapIds(int amountToReserve, World world) {
        List<MapIdComponent> newMapIds = new ArrayList<>();
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
