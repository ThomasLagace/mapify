package me.thomaslagace.mapify.minecraft.rightclick;

import java.util.List;

import me.thomaslagace.mapify.config.Config;
import me.thomaslagace.mapify.minecraft.MinecraftImage;
import me.thomaslagace.mapify.ModRegisterable;
import me.thomaslagace.mapify.minecraft.MinecraftImagesDatabase;
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

public class ItemFrameRightClickHandler implements ModRegisterable {
    private final MinecraftImagesDatabase imagesDatabase;

    public ItemFrameRightClickHandler(MinecraftImagesDatabase imagesDatabase) {
        this.imagesDatabase = imagesDatabase;
    }

    public void register() {
        UseEntityCallback.EVENT.register(this::run);
    }

    private ActionResult run(PlayerEntity playerEntity, World world, Hand hand, Entity entity, EntityHitResult entityHitResult) {
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

        MinecraftImage minecraftImage = imagesDatabase.getMinecraftImageById(usedItemNbtCompound.getUuid("mapify.id"));

        if (minecraftImage == null) {
            playerEntity.sendMessage(Text.of(Config.MAP_NOT_FOUND), true);
            return ActionResult.FAIL;
        }

        int width = minecraftImage.getMinecraftMapIds()[0].length;
        int height = minecraftImage.getMinecraftMapIds().length;

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

        ItemStack[][] mapItems = minecraftImage.getDisplayMaps();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                itemFrames[y][x].setHeldItemStack(mapItems[y][x]);
                itemFrames[y][x].setRotation(0);
            }
        }

        return ActionResult.SUCCESS;
    }

    private ItemFrameEntity[][] getAdjacentItemFrames(World world, BlockPos pos, Direction direction, int width, int height) {
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
