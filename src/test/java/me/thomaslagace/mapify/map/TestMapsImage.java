package me.thomaslagace.mapify.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.Test;

public class TestMapsImage {
    @Test
    public void testMapsImage() throws Exception {
        int[] minecraftMapIds = new int[1];
        UUID playerId = UUID.randomUUID();
        URL url = new URI("https://google.com").toURL();

        MapsImage mapsImage = new MapsImage(playerId, "SomeName", url, 1, 1, minecraftMapIds);

        assertEquals(playerId, mapsImage.getCreatorId());
        assertEquals("SomeName", mapsImage.getName());
        assertEquals(url, mapsImage.getUrl());
        assertEquals(1, mapsImage.getWidth());
        assertEquals(1, mapsImage.getHeight());
        assertEquals(minecraftMapIds, mapsImage.getMinecraftMapIds());
    }
}
