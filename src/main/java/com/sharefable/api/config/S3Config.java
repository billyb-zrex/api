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
    private static final String DATA_FILE_NAME = "index.json";
    private static final String PATH_FOR_COMMON_ASSET = "/cmn";
    private static final String PATH_FOR_PROXY_ASSET = "/proxy_asset";
    private static final String PATH_FOR_SCREEN_ASSET = "/srn/%s";
    private static final String PATH_FOR_FLOW_ASSET = "/flo/%s";
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
        return getQualifiedPathFor(type, "0", filePath);
    }

    public PathConfigForClient getPathConfigForClient() {
        AssetFilePath assetFilePath = getAssetFilePathWithCommonProps();
        return new PathConfigForClient(
            AssetFilePath.from(assetFilePath, getPrefixPath(AssetType.Common, "") + "/").getS3UriToFile(),
            AssetFilePath.from(assetFilePath, getPrefixPath(AssetType.Screen, "")).getS3UriToFile(),
            AssetFilePath.from(assetFilePath, getPrefixPath(AssetType.Flow, "")).getS3UriToFile()
        );
    }

    public FileNames getFileNames() {
        return new FileNames(DATA_FILE_NAME);
    }


    private String getPrefixPath(AssetType type, String prefix) {
        String path = getPathForAssetType(type);
        return rootQualifier + String.format(path, prefix);
    }

    private AssetFilePath getAssetFilePathWithCommonProps() {
        AssetFilePath assetFilePath = new AssetFilePath();
        assetFilePath.setBucketName(assetBucketName);
        assetFilePath.setRegionName(region);
        return assetFilePath;
    }

    public AssetFilePath getQualifiedPathFor(AssetType type, String prefix, String filePath) {
        AssetFilePath assetFilePath = getAssetFilePathWithCommonProps();
        String prefixPath = getPrefixPath(type, prefix);
        assetFilePath.setPrefixPathForType(prefixPath);
        assetFilePath.setFilePath(filePath);
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

    public record PathConfigForClient(String commonAsset, String screenAsset, String flowAsset) {
    }

    public record FileNames(String dataFile) {
    }
}
