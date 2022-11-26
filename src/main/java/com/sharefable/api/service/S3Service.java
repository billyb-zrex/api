package com.sharefable.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.sharefable.api.config.S3Config;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;

@Service
public class S3Service {
    private static final String PATH_APP_GENERIC_ASSET = "/app/gen";
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
            prefixPath = PATH_APP_GENERIC_ASSET;
        }

        return prefixPath + StringUtils.prependIfMissing(filePath, "/");
    }

    public void upload(String filePath, AssetType type, byte[] content) {
        ObjectMetadata meta = new ObjectMetadata();
        if (type != AssetType.AppGeneric) {
            meta.addUserMetadata("Content-Type", type.value);
        }

        PutObjectRequest req = new PutObjectRequest(
            config.getAppBucketName(),
            getCanonicalFilePath(filePath, type),
            new ByteArrayInputStream(content),
            meta);
        client.putObject(req);
    }

    public byte[] getObjectContent(String fileName) throws IOException {
        GetObjectRequest req = new GetObjectRequest(
            config.getAppBucketName(),
            fileName
        );
        S3Object object = client.getObject(req);
        S3ObjectInputStream content = object.getObjectContent();
        byte[] fileAsBytes = IOUtils.toByteArray(content);
        content.close();
        return fileAsBytes;
    }

    enum AssetType {
        Asset("project_asset"),
        AppGeneric("generic_app_assets");

        public final String value;

        AssetType(String value) {
            this.value = value;
        }
    }
}

