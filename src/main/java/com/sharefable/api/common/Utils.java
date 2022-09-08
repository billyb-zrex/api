package com.sharefable.api.common;

import com.sharefable.api.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.api.entity.AssetMapping;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;
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

    static boolean isText(MediaType media) {
        return media.getType().equalsIgnoreCase("text")
            || media.getType().equalsIgnoreCase("application");
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

    // Nearest map is the map from the list of maps, which is most similar with the matchWith map
    // Similarity is figured out by checking if the key is present in both the map and the value is same or not
    static int getNearestMap(List<Map<String, String>> maps, Map<String, String> matchWith) {
        int largestScore = -1;
        int largestScoreIndex = -1;
        for (int i = 0; i < maps.size(); i++) {
            Map<String, String> map = maps.get(i);
            int score = 0;
            if (map != null) {
                for (String key : map.keySet()) {
                    if (matchWith.containsKey(key)) {
                        score += 1;
                        if (map.get(key).equals(matchWith.get(key))) {
                            score += 1;
                        }
                    }
                }
            }
            if (score > largestScore) {
                largestScore = score;
                largestScoreIndex = i;
            }
        }

        return largestScoreIndex;
    }

    static String getAssetNameFromAssetPath(String assetPath) {
        return assetPath.substring(0, Math.min(255, assetPath.length()));
    }

    static boolean isStrEmpty(String str) {
        return str == null || str.trim().equals("");
    }
}
