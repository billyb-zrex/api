package com.sharefable.api.config;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.sharefable.api.common.AssetFilePath;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConfigurationProperties(prefix = "com.sharefable.api.s3")
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
public class S3Config {
    private static final String DATA_FILE_NAME = "index.json";
    private static final String EDIT_FILE_NAME = "edits.json";
    private static final String IMAGE_FILE_NAME = "index.img";
    private static final String PATH_FOR_COMMON_ASSET = "/cmn";
    private static final String PATH_FOR_PROXY_ASSET = "/proxy_asset";
    private static final String PATH_FOR_SCREEN_ASSET = "/srn/%s";
    private static final String PATH_FOR_TOUR_ASSET = "/tour/%s";
    private static final String PATH_FOR_USER_UPLOADED_ASSET = "/usr/org/%s";
    private String accessKeyId;
    private String accessKeySecret;
    private String region;
    private String rootQualifier;
    private String assetBucketName;

    @Autowired
    private Environment env;

    public static EntityFilesConfig getEntityFiles() {
        return new EntityFilesConfig(
            // TODO fix this, for screen datafile needs to be cached
            //      for tour data file need not be cached
            new FileConfig(DATA_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(EDIT_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(IMAGE_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache));
    }

    private String getPathForAssetType(AssetType type) {
        return switch (type) {
            case ProxyAsset -> PATH_FOR_PROXY_ASSET;
            case Tour -> PATH_FOR_TOUR_ASSET;
            case Screen -> PATH_FOR_SCREEN_ASSET;
            case Common -> PATH_FOR_COMMON_ASSET;
            case UserGenerated -> PATH_FOR_USER_UPLOADED_ASSET;
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
            AssetFilePath.from(assetFilePath, getPrefixPath(AssetType.Tour, "")).getS3UriToFile()
        );
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

    private boolean isLocal() {
        String[] activeProfiles = env.getActiveProfiles();
        for (String activeProfile : activeProfiles) {
            if (StringUtils.equalsIgnoreCase(activeProfile, "dev")) {
                return true;
            }
        }
        return false;
    }

    @Bean
    AmazonS3 s3Client() {
        return AmazonS3ClientBuilder.standard().withRegion(region).build();

//        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard().withRegion(region);

//        if (!isLocal()) {
//            log.info("Building s3 client using iam role");
//            return builder.build();
//        }
//
//        log.info("Building s3 client using user access keys");
//        final BasicAWSCredentials basicAwsCredentials = new BasicAWSCredentials(accessKeyId, accessKeySecret);
//        return builder
//            .withCredentials(new AWSStaticCredentialsProvider(basicAwsCredentials))
//            .build();
    }

    public enum AssetType {
        ProxyAsset,
        Screen,
        Tour,
        Common,
        UserGenerated
    }

    public enum DATA_FILE_CACHE_POLICY {
        Default,
        NoCache,
    }

    public record PathConfigForClient(String commonAsset, String screenAsset, String tourAsset) {
    }

    public record FileConfig(String filename, DATA_FILE_CACHE_POLICY cachePolicy) {
    }

    public record EntityFilesConfig(FileConfig dataFile, FileConfig editFile, FileConfig imgFile) {
    }
}
