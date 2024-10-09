package me.thomaslagace.mapify.events.rightclick;

import com.mojang.serialization.MapDecoder;
import me.thomaslagace.mapify.Mapify;
import me.thomaslagace.mapify.map.MapImage;
import me.thomaslagace.mapify.map.MapsManager;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemFrameRightClickHandler {
    public static void register() {
        UseEntityCallback.EVENT.register(ItemFrameRightClickHandler::run);
    }

    private static ActionResult run(PlayerEntity playerEntity, World world, Hand hand, Entity entity, @Nullable EntityHitResult entityHitResult) {
        if (!(entity instanceof ItemFrameEntity itemFrame)) {
            return ActionResult.PASS;
        }

        ItemStack usedItem = playerEntity.getStackInHand(hand);
        NbtComponent usedItemNbt = usedItem.get(DataComponentTypes.CUSTOM_DATA);

        if (usedItemNbt == null || usedItemNbt.isEmpty()) {
            return ActionResult.PASS;
        }

        NbtCompound usedItemNbtCompound = usedItemNbt.getNbt();

        if (!usedItemNbtCompound.getBoolean("mapify")) {
            return ActionResult.PASS;
        }

        MapImage mapImage = MapsManager.getMapImageById(usedItemNbtCompound.getInt("id"));

        if (mapImage == null) {
            playerEntity.sendMessage(Text.of("Map not found."), true);
            return ActionResult.PASS;
        }

        int width = usedItemNbtCompound.getInt("width");
        int height = usedItemNbtCompound.getInt("height");

        ItemFrameEntity[][] itemFrames = new ItemFrameEntity[width][height];

        Direction direction = itemFrame.getHorizontalFacing();
        BlockPos itemFramePos = itemFrame.getBlockPos();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                BlockPos checkPos = itemFramePos.offset(direction.rotateYClockwise(), -i).offset(Direction.UP, j);
                List<ItemFrameEntity> checkFrames = world.getEntitiesByClass(ItemFrameEntity.class, new Box(checkPos), (frameEntity) -> frameEntity.getBlockPos().equals(checkPos));
                for (ItemFrameEntity checkFrame : checkFrames) {
                    if (checkFrame.getHorizontalFacing() == direction) {
                        itemFrames[i][j] = checkFrame;
                    }
                }

                if (itemFrames[i][j] == null) {
                    playerEntity.sendMessage(Text.of("Not enough item frames to place the map."), true);
                    return ActionResult.PASS;
                }
            }
        }

        ItemStack[][] mapItems = mapImage.getMapsAsItemStacks();

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                Mapify.LOGGER.info("i: " + i + " j: " + j + "  :  " + mapItems[i][j].get(DataComponentTypes.MAP_ID).toString());
            }
        }

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                itemFrames[i][j].setHeldItemStack(mapItems[i][j]);
            }
        }

        return ActionResult.SUCCESS;
    }
}
