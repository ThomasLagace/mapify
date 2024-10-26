package me.thomaslagace.mapify.map;

import java.net.URL;
import me.thomaslagace.mapify.image.ImageUtil;
import org.junit.jupiter.api.Test;

public class TestImageUtil {
    @Test
    public void test() throws Exception {
        URL url = new URL("https://fastly.picsum.photos/id/952/1280/720.jpg?hmac=OfAUgnCaCjmmlx2EkYZitHG9WDJxYFq2S8mF_W9lVzM");
        ImageUtil.getImageFromURL(url, new URL[] {}, 10000);
    }
}
