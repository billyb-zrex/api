package com.sharefable.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import com.sharefable.api.common.AssetFilePath;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class S3Service {

    private final AmazonS3 client;

    @Autowired
    S3Service(AmazonS3 s3) {
        this.client = s3;
    }

    public AssetFilePath upload(AssetFilePath filePath, byte[] content) {
        return upload(filePath, content, new HashMap<>());
    }

    public AssetFilePath upload(AssetFilePath filePath, byte[] content, Map<String, String> assetMetadata) {
        ObjectMetadata meta = new ObjectMetadata();
        String contentType;
        if ((contentType = assetMetadata.get(HttpHeaders.CONTENT_TYPE)) != null) {
            meta.setContentType(contentType);
            assetMetadata.remove(HttpHeaders.CONTENT_TYPE);
        }
        String contentEncoding;
        if ((contentEncoding = assetMetadata.get(HttpHeaders.CONTENT_ENCODING)) != null) {
            meta.setContentEncoding(contentEncoding);
            assetMetadata.remove(HttpHeaders.CONTENT_ENCODING);
        }

        for (Map.Entry<String, String> metadata : assetMetadata.entrySet()) {
            meta.addUserMetadata(metadata.getKey(), metadata.getValue());
        }

        PutObjectRequest req = new PutObjectRequest(
            filePath.getBucketName(),
            filePath.getFullQualifiedPath(),
            new ByteArrayInputStream(content),
            meta);
        client.putObject(req);

        return filePath;
    }

    public AssetFilePath copy(AssetFilePath fromObject, AssetFilePath toObject) {
        CopyObjectRequest req = new CopyObjectRequest(
            fromObject.getBucketName(),
            fromObject.getFullQualifiedPath(),
            toObject.getBucketName(),
            toObject.getFullQualifiedPath());
        client.copyObject(req);
        return toObject;
    }

    public byte[] getObjectContent(AssetFilePath filePath) throws IOException {
        GetObjectRequest req = new GetObjectRequest(
            filePath.getBucketName(),
            filePath.getFullQualifiedPath()
        );
        S3Object object = client.getObject(req);
        S3ObjectInputStream content = object.getObjectContent();
        byte[] fileAsBytes = IOUtils.toByteArray(content);
        content.close();
        return fileAsBytes;
    }
}

