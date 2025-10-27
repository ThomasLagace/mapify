package me.thomaslagace.mapify.map;

import me.thomaslagace.mapify.minecraft.MinecraftImageUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests the MinecraftColorMapUtil class.
 */
public class TestMinecraftColorMapUtil {
    @Test
    public void testColorMap() {
        // Function for the color map is : ID * 4 + Brightness
        // Where ID is the id of the color
        // Where Brightness is the brightness of the color (0 = Low, 1 = Normal, 2 = High, 3 = Lowest)

        // Expected color of Snow that is 255, 255, 255 at High brightness (8 * 4 + 2 = 34)
        assertEquals(34, MinecraftImageUtils.getClosestRenderColor(255, 255, 255, 255));

        // Expected color of Fire that is 255, 0, 0 at High brightness (4 * 4 + 2 = 18)
        assertEquals(18, MinecraftImageUtils.getClosestRenderColor(255, 0, 0, 255));

        // Expected color of Plant that is 0, 124, 0 at High brightness (7 * 4 + 2 = 30)
        assertEquals(30, MinecraftImageUtils.getClosestRenderColor(0, 124, 0, 255));

        // Expected color of Water that is 64, 64, 255 at High brightness (12 * 4 + 2 = 50)
        assertEquals(50, MinecraftImageUtils.getClosestRenderColor(64, 64, 255, 255));
    }
}
