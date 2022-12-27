package com.sharefable.api.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.sharefable.api.common.AssetFilePath;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.sharefable.api.s3")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class S3Config {
    private final String PATH_FOR_COMMON_ASSET = "/cmn";
    private final String PATH_FOR_PROXY_ASSET = "/proxy_asset";
    private final String PATH_FOR_SCREEN_ASSET = "/srn/%d";
    private final String PATH_FOR_FLOW_ASSET = "/flo/%d";
    private String accessKeyId;
    private String accessKeySecret;
    private String region;
    private String rootQualifier;
    private String assetBucketName;

    private String getPathForAssetType(AssetType type) {
        return switch (type) {
            case ProxyAsset -> PATH_FOR_PROXY_ASSET;
            case Flow -> PATH_FOR_FLOW_ASSET;
            case Screen -> PATH_FOR_SCREEN_ASSET;
            case Common -> PATH_FOR_COMMON_ASSET;
        };
    }

    public AssetFilePath getQualifiedPathFor(AssetType type, String filePath) {
        return getQualifiedPathFor(type, 0L, filePath);
    }

    public AssetFilePath getQualifiedPathFor(AssetType type, Long id, String filePath) {
        String path = getPathForAssetType(type);
        String prefixPath = rootQualifier + String.format(path, id);

        AssetFilePath assetFilePath = new AssetFilePath();
        assetFilePath.setBucketName(assetBucketName);
        assetFilePath.setFilePath(filePath);
        assetFilePath.setRegionName(region);
        assetFilePath.setPrefixPathForType(prefixPath);
        assetFilePath.setFullQualifiedPath(prefixPath + StringUtils.prependIfMissing(filePath, "/"));
        return assetFilePath;
    }

    @Bean
    AmazonS3 s3Client() {
        final BasicAWSCredentials basicAwsCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
        return AmazonS3ClientBuilder.standard()
            .withRegion(region)
            .withCredentials(new AWSStaticCredentialsProvider(basicAwsCredentials))
            .build();
    }

    public enum AssetType {
        ProxyAsset,
        Screen,
        Flow,
        Common,
    }
}
