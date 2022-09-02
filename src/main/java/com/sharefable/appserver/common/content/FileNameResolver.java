package com.sharefable.appserver.common.content;

import com.sharefable.appserver.common.Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.StringJoiner;

@Slf4j
public class FileNameResolver {
    private final URL origin;
    private final URL url;
    @Getter
    private final String fileName;

    public static final String INDEX_FILE = "index";

    public FileNameResolver(URL origin, URL url) {
        this.origin = origin;
        this.url = url;
        fileName = genFileName();
    }

    private String qualifiedFileName(String name) {
        return "assets/" + name;
    }

    private String namePostfix(String name) {
        return name + "_"  + Utils.getShortUUID();
    }

    private String normalizePathSeparator(String path) {
        if (path.equals("")) {
            return path;
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        return path;
    }

    private boolean isOriginAndUrlSame() {
        if (origin.sameFile(url)) {
            return true;
        }
        String originPath = normalizePathSeparator(origin.getPath());
        String urlPath = normalizePathSeparator(url.getPath());
        return originPath.equals(urlPath);
    }

    private String genFileName() {
        String urlPath = normalizePathSeparator(url.getPath());
        String[] urlPathSplit = urlPath.split("/");
        String extension = "";
        StringJoiner joiner = new StringJoiner("_");

        for (int i = 0; i < urlPathSplit.length; i++) {
            if (i == urlPathSplit.length - 1) {
                String lastPath = urlPathSplit[i];
                if (lastPath.contains(".")) {
                    String[] lastPathSplit = lastPath.split("\\.");
                    StringJoiner sb1 = new StringJoiner("_");
                    for (int i1 = 0; i1 < lastPathSplit.length; i1++) {
                        if (i1 == lastPathSplit.length -1 ) {
                            extension = lastPathSplit[i1];
                        } else {
                            sb1.add(lastPathSplit[i1]);
                        }
                    }
                    lastPath = sb1.toString();
                }
                joiner.add(lastPath);
            } else {
                joiner.add(urlPathSplit[i]);
            }
        }

        String escapedPath = joiner.toString();
        if (isOriginAndUrlSame()) {
            escapedPath = escapedPath.isEmpty() ? INDEX_FILE : escapedPath + "_" + INDEX_FILE;
        }
        String fileName = namePostfix(escapedPath);
        if (!extension.equals("")) {
            fileName += "." + extension;
        }

        return qualifiedFileName(fileName);
    }
}
