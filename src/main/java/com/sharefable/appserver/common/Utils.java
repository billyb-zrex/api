package com.sharefable.appserver.common;

public interface Utils {
    static String normalizeProjectName(String name) {
        // 255 because name column is db is of varchar(255) length
        String lcName = name.substring(0, Math.min(255, name.length())).toLowerCase();
        return lcName.replaceAll("[\\W]+|_", "_");
    }
}
