package com.sharefable.appserver.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.sharefable.appserver.config.S3Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
public class S3Service {
    private final AmazonS3 client;
    private final S3Config config;

    @Autowired
    S3Service(AmazonS3 s3, S3Config config) {
        this.client = s3;
        this.config = config;
    }

    public void upload(String fileName, String contentType, String content) {
        ObjectMetadata meta = new ObjectMetadata();
        meta.addUserMetadata("Content-Type", contentType);
        PutObjectRequest req = new PutObjectRequest(
            config.getProxyAssetBucketName(),
            fileName,
            new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
            meta);
        client.putObject(req);
    }
}
