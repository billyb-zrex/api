package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.ImageType;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

@Slf4j
public abstract class ServiceBase {
    private final S3Service s3Service;
    private final S3Config s3Config;

    protected ServiceBase(S3Service s3Service, S3Config s3Config) {
        this.s3Service = s3Service;
        this.s3Config = s3Config;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    Optional<AssetFilePath> uploadBase64ImageToS3(String imageData, S3Config.AssetType assetType) {
        Pair<byte[], ImageType> imgDataAndType = Utils.getImageDataFromBase64Str(imageData);

        if (imgDataAndType.getValue1() == ImageType.Unknown) {
            log.error("Can't find type from image data. Only allowed type is png. Skipping saving of image.");
            return Optional.empty();
        }

        String filePath = UUID.randomUUID() + "." + imgDataAndType.getValue1().type;
        AssetFilePath assetFilePath = s3Config.getQualifiedPathFor(assetType, filePath);
        s3Service.upload(assetFilePath, imgDataAndType.getValue0());
        return Optional.ofNullable(assetFilePath);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    AssetFilePath uploadDataFileToS3(String content, String prefixHash, String fileName, S3Config.AssetType assetType) {
        AssetFilePath assetFilePath = s3Config.getQualifiedPathFor(assetType, prefixHash, fileName);
        s3Service.upload(assetFilePath, content.getBytes(StandardCharsets.UTF_8));
        return assetFilePath;
    }
}
