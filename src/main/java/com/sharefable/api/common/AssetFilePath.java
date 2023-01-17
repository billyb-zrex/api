package com.sharefable.api.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetFilePath {
    String regionName;
    String bucketName;
    // Full path to the file without the bucket name; like root/srn/0/data/index.json
    String fullQualifiedPath;
    // Prefix path to the file based on asset type; like for screen root/srn/0
    String prefixPathForType;
    // File path after the prefix path data/index.json
    String filePath;

    public static AssetFilePath from(AssetFilePath halfConstructedPath, String qualifiedPath) {
        return new AssetFilePath(halfConstructedPath.regionName, halfConstructedPath.bucketName, qualifiedPath, "", "");
    }

    public static AssetFilePath from(AssetFilePath assetFilePath) {
        return new AssetFilePath(
            assetFilePath.regionName,
            assetFilePath.bucketName,
            assetFilePath.fullQualifiedPath,
            assetFilePath.prefixPathForType,
            assetFilePath.filePath
        );
    }

    public String getS3UriToFile() {
        return "https://" + bucketName + ".s3." + regionName + ".amazonaws.com/" + fullQualifiedPath;
    }
}
