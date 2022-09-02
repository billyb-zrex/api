package com.sharefable.appserver.common;

import com.sharefable.appserver.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.appserver.entity.AssetMapping;
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

    // In order to check the new asset with the saved one currently we check if the asset url, http status, http method
    // and query parameters are same
    // Two assets /app/home?a=1&ts=45903485 and /app/home?a=1&ts=435490438 are different even if `ts` is ignored in the
    // logic.
    static boolean isSavedAssetIsSameWithNewAsset(AssetMapping savedAsset, NewProxyAssetReqBodyParsed inAsset) {
        return savedAsset.getAssetPath().equals(inAsset.getUrl().getPath())
            && savedAsset.getStatus() == inAsset.getStatus()
            && savedAsset.getMethod() == inAsset.getMethod()
            && savedAsset.getQueryParams().equals(inAsset.getQueryParams());
    }
}
