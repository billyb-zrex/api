package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.ImageType;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.S3Config;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.javatuples.Pair;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public abstract class ServiceBase {
    private static final String PATH_TO_SCHEMA_FILE_FOR_TOUR_INDEX = "/data-schema/v=%s/tour/index.json";
    private static final String PATH_TO_SCHEMA_FILE_FOR_SCREEN_EDIT = "/data-schema/v=%s/screen/edits.json";

    private final S3Service s3Service;
    private final S3Config s3Config;
    private final AppSettings settings;

    protected ServiceBase(AppSettings settings, S3Service s3Service, S3Config s3Config) {
        this.s3Service = s3Service;
        this.s3Config = s3Config;
        this.settings = settings;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    Optional<AssetFilePath> uploadBase64ImageToS3(String imageData, S3Config.AssetType assetType) {
        Pair<byte[], ImageType> imgDataAndType = Utils.getImageDataFromBase64Str(imageData);

        if (imgDataAndType.getValue1() == ImageType.Unknown) {
            log.error("Can't find type from image data. Only allowed type is png. Skipping saving of image.");
            return Optional.empty();
        }

        String contentType = switch (imgDataAndType.getValue1()) {
            case PNG -> MediaType.IMAGE_PNG_VALUE;
            case JPEG -> MediaType.IMAGE_JPEG_VALUE;
            default -> MediaType.ALL_VALUE;
        };

        Map<String, String> userDefinedMetadata = new HashMap<>(1);
        userDefinedMetadata.put(HttpHeaders.CONTENT_TYPE, contentType);

        String filePath = UUID.randomUUID() + "." + imgDataAndType.getValue1().type;
        AssetFilePath assetFilePath = s3Config.getQualifiedPathFor(assetType, filePath);
        s3Service.upload(assetFilePath, imgDataAndType.getValue0(), userDefinedMetadata);
        return Optional.ofNullable(assetFilePath);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    protected AssetFilePath uploadTemplateFileToS3(String prefixHash, DATA_FILE_TYPE type) {
        String schemaVersion = settings.currentSchemaVersion().toValue();
        TemplateFile tFile = switch (type) {
            case TOUR_INDEX -> new TemplateFile(
                String.format(PATH_TO_SCHEMA_FILE_FOR_TOUR_INDEX, schemaVersion),
                S3Config.AssetType.Tour,
                s3Config.getFileNames().dataFile()
            );

            case SCREEN_EDIT -> new TemplateFile(
                String.format(PATH_TO_SCHEMA_FILE_FOR_SCREEN_EDIT, schemaVersion),
                S3Config.AssetType.Screen,
                s3Config.getFileNames().editFile()
            );
        };
        try (InputStream resourceAsStream = getClass().getResourceAsStream(tFile.fromPath())) {
            if (resourceAsStream == null) {
                log.error("No default data file is present while creating tour. Can't find schema file with path = {}", tFile.fromPath);
                throw new RuntimeException("Can't find schema file");
            }
            String fileContent = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            return uploadDataFileToS3(fileContent, prefixHash, tFile.toFileName(), tFile.type());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    AssetFilePath uploadDataFileToS3(String content, String prefixHash, String fileName, S3Config.AssetType assetType) {
        AssetFilePath assetFilePath = s3Config.getQualifiedPathFor(assetType, prefixHash, fileName);
        Map<String, String> userDefinedMetadata = new HashMap<>(1);
        userDefinedMetadata.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        s3Service.upload(assetFilePath, content.getBytes(StandardCharsets.UTF_8), userDefinedMetadata);
        return assetFilePath;
    }

    public enum DATA_FILE_TYPE {
        TOUR_INDEX,
        SCREEN_EDIT
    }

    private record TemplateFile(String fromPath, S3Config.AssetType type, String toFileName) {
    }
}
