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
    String bucketName;
    // Full path to the file without the bucket name; like root/srn/0/data/index.json
    String fullQualifiedPath;
    // Prefix path to the file based on asset type; like for screen root/srn/0
    String prefixPathForType;
    // File path after the prefix path data/index.json
    String filePath;

    public String getS3UriToFile() {
        return "s3://" + bucketName + "/" + fullQualifiedPath;
    }
}
