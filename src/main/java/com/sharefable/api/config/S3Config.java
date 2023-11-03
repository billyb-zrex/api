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
    private static final String LOADER_FILE_NAME = "loader.json";
    private static final String IMAGE_FILE_NAME = "index.img";
    private static final String PUBLISHED_DATA_FILE_NAME = "0_index.json";
    private static final String PUBLISHED_EDIT_FILE_NAME = "0_edits.json";
    private static final String PUBLISHED_LOADER_FILE_NAME = "0_loader.json";
    private static final String PUBLISHED_TOUR_ENTITY_FILE_NAME = "0_d_data.json";
    private static final String MANIFEST_FILE = "manifest.json";
    private static final String PATH_FOR_COMMON_ASSET = "/cmn";
    private static final String PATH_FOR_PROXY_ASSET = "/proxy_asset";
    private static final String PATH_FOR_PUBLISHED_TOUR_ASSET = "/ptour/%s";
    private static final String PATH_FOR_SCREEN_ASSET = "/srn/%s";
    private static final String PATH_FOR_TOUR_ASSET = "/tour/%s";
    private static final String PATH_FOR_USER_UPLOADED_ASSET = "/usr/org/%s";
    private String region;
    private String rootQualifier;
    private String assetBucketName;
    private String cdn;

    @Autowired
    private Environment env;

    public static EntityFilesConfig getEntityFiles() {
        return new EntityFilesConfig(
            // TODO fix this, for screen datafile needs to be cached
            //      for tour data file need not be cached
            new FileConfig(DATA_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(EDIT_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(LOADER_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(IMAGE_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(PUBLISHED_DATA_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(PUBLISHED_EDIT_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(PUBLISHED_LOADER_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(PUBLISHED_TOUR_ENTITY_FILE_NAME, DATA_FILE_CACHE_POLICY.NoCache),
            new FileConfig(MANIFEST_FILE, DATA_FILE_CACHE_POLICY.NoCache));
    }

    private String getPathForAssetType(AssetType type) {
        return switch (type) {
            case ProxyAsset -> PATH_FOR_PROXY_ASSET;
            case Tour -> PATH_FOR_TOUR_ASSET;
            case Screen -> PATH_FOR_SCREEN_ASSET;
            case Common -> PATH_FOR_COMMON_ASSET;
            case PublishedTour -> PATH_FOR_PUBLISHED_TOUR_ASSET;
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
            AssetFilePath.from(assetFilePath, getPrefixPath(AssetType.Tour, "")).getS3UriToFile(),
            AssetFilePath.from(assetFilePath, getPrefixPath(AssetType.PublishedTour, "")).getS3UriToFile()
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
        assetFilePath.setCdn(cdn);
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
        return AmazonS3ClientBuilder.standard().withRegion(region).build();
    }

    public enum AssetType {
        ProxyAsset,
        Screen,
        Tour,
        Common,
        UserGenerated,
        PublishedTour
    }

    public enum DATA_FILE_CACHE_POLICY {
        Default,
        NoCache,
    }

    public record PathConfigForClient(
        String commonAsset,
        String screenAsset,
        String tourAsset,
        String tourPublishedAsset) {
    }

    public record FileConfig(String filename, DATA_FILE_CACHE_POLICY cachePolicy) {
    }

    public record EntityFilesConfig(
        FileConfig dataFile,
        FileConfig editFile,
        FileConfig loaderFile,
        FileConfig imgFile,
        FileConfig publishedDataFile,
        FileConfig publishedEditFile,
        FileConfig publishedLoaderFile,
        FileConfig publishedTourEntityFile,
        FileConfig manifestFile) {
    }
}
