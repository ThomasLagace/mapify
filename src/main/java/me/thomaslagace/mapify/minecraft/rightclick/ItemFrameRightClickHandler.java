package me.thomaslagace.mapify.minecraft.rightclick;

import java.util.List;
import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.minecraft.ImageMinecraft;
import me.thomaslagace.mapify.Mapify;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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

        if (itemFrame.getHeldItemStack().getItem() != Items.AIR) {
            return ActionResult.FAIL;
        }

        ImageMinecraft imageMinecraft = Mapify.imageManager.getMapImageById(usedItemNbtCompound.getInt("mapify.id"));

        if (imageMinecraft == null) {
            playerEntity.sendMessage(Text.of(Config.MAP_NOT_FOUND), true);
            return ActionResult.FAIL;
        }

        int width = usedItemNbtCompound.getInt("mapify.width");
        int height = usedItemNbtCompound.getInt("mapify.height");

        Direction direction = itemFrame.getHorizontalFacing();
        BlockPos itemFramePos = itemFrame.getBlockPos();

        ItemFrameEntity[][] itemFrames = getAdjacentItemFrames(world, itemFramePos, direction, width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (itemFrames[y][x] == null) {
                    playerEntity.sendMessage(Text.of(Config.ITEM_FRAME_NOT_FOUND), true);
                    return ActionResult.FAIL;
                } else if (itemFrames[y][x].getHeldItemStack().getItem() != Items.AIR) {
                    playerEntity.sendMessage(Text.of(Config.ITEM_FRAMES_NOT_EMPTY), true);
                    return ActionResult.FAIL;
                }
            }
        }

        ItemStack[][] mapItems = imageMinecraft.getMapItemStack();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                itemFrames[y][x].setHeldItemStack(mapItems[y][x]);
                itemFrames[y][x].setRotation(0);
            }
        }

        return ActionResult.SUCCESS;
    }

    private static ItemFrameEntity[][] getAdjacentItemFrames(World world, BlockPos pos, Direction direction, int width, int height) {
        ItemFrameEntity[][] itemFrames = new ItemFrameEntity[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                BlockPos itemFramePos = pos.offset(direction.rotateYClockwise(), -x).offset(Direction.UP, -y);
                List<ItemFrameEntity> itemFrameEntities = world.getEntitiesByClass(ItemFrameEntity.class, new Box(itemFramePos), (entity) -> entity.getBlockPos().equals(itemFramePos));

                for (ItemFrameEntity itemFrameEntity : itemFrameEntities) {
                    if (itemFrameEntity.getHorizontalFacing() == direction) {
                        itemFrames[y][x] = itemFrameEntity;
                    }
                }
            }
        }

        return itemFrames;
    }
}
