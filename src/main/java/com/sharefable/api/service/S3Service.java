package com.sharefable.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.sharefable.api.common.Consts;
import com.sharefable.api.config.S3Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/*
 * Location details
 *  /app/gen <- All general assets (images per entity like org or user etc)
 *      ...
 *  /app/project_data
 *      /{id}
 *          /proxy_asset
 *          /data_files
 *          ...
 */

@Service
public class S3Service {

    private final AmazonS3 client;
    private final S3Config config;

    @Autowired
    S3Service(AmazonS3 s3, S3Config config) {
        this.client = s3;
        this.config = config;
    }

    public String getCanonicalFilePath(String filePath, AssetType type) {
        String prefixPath = "";
        if (type == AssetType.AppGeneric) {
            prefixPath = Consts.PATH_APP_GENERIC_ASSET;
        } else if (type == AssetType.Project) {
            prefixPath = Consts.PATH_PROJECT_SPECIFIC_ASSET;
        }

        return "lin_" + config.getFilePathQualifier() + prefixPath + StringUtils.prependIfMissing(filePath, "/");
    }

    public String upload(String filePath, AssetType type, byte[] content) {
        String fullQualifiedFilePath = getCanonicalFilePath(filePath, type);
        ObjectMetadata meta = new ObjectMetadata();
        PutObjectRequest req = new PutObjectRequest(
            config.getAppBucketName(),
            fullQualifiedFilePath,
            new ByteArrayInputStream(content),
            meta);
        client.putObject(req);
        return fullQualifiedFilePath;
    }

    public byte[] getObjectContent(String filePath, AssetType type) throws IOException {
        GetObjectRequest req = new GetObjectRequest(
            config.getAppBucketName(),
            getCanonicalFilePath(filePath, type)
        );
        S3Object object = client.getObject(req);
        S3ObjectInputStream content = object.getObjectContent();
        byte[] fileAsBytes = IOUtils.toByteArray(content);
        content.close();
        return fileAsBytes;
    }

    enum AssetType {
        Project, AppGeneric
    }
}

