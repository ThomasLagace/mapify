package me.thomaslagace.mapify.config;

import java.net.URL;

public class MapifyUtils {
    public static boolean isUrlAllowed(URL url) {
        if (Config.ALLOWED_IMAGE_DOMAINS.isEmpty()) {
            return true;
        }

        for (String allowedDomain : Config.ALLOWED_IMAGE_DOMAINS) {
            if (url.getHost().equals(allowedDomain)) {
                return true;
            }
        }

        return false;
    }
}
