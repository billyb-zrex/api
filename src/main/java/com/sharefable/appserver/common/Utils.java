package com.sharefable.appserver.common;

import org.springframework.http.MediaType;

import java.util.UUID;

public interface Utils {
    static String normalizeProjectName(String name) {
        // 255 because name column is db is of varchar(255) length
        String lcName = name.substring(0, Math.min(255, name.length())).toLowerCase();
        return lcName.replaceAll("[\\W]+|_", "_");
    }

    static boolean isHtml(MediaType media) {
        return media.getSubtype().equalsIgnoreCase("html");
    }

    // https://stackoverflow.com/a/17183247
    static String getShortUUID() {
        return Long
            .toString(UUID.randomUUID().getLeastSignificantBits(), Character.MAX_RADIX)
            .replaceAll("-", "");
    }
}
