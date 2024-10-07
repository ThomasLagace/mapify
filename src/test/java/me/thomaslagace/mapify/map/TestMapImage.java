package me.thomaslagace.mapify.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URL;
import java.util.UUID;
import net.minecraft.component.type.MapIdComponent;
import org.junit.jupiter.api.Test;

/**
 * Tests the MapsImage class.
 */
public class TestMapImage {
    @Test
    public void testMapsImage() throws Exception {
        MapIdComponent[][] minecraftMapIds = new MapIdComponent[1][1];
        UUID playerId = UUID.randomUUID();
        URL url = new URI("https://google.com").toURL();

        MapImage mapImage = new MapImage(playerId, "SomeName", url, 1, 1, minecraftMapIds);

        assertEquals(playerId, mapImage.getCreatorId());
        assertEquals("SomeName", mapImage.getName());
        assertEquals(url, mapImage.getUrl());
        assertEquals(1, mapImage.getWidth());
        assertEquals(1, mapImage.getHeight());
        assertEquals(minecraftMapIds, mapImage.getMinecraftMapIds());
    }
}
