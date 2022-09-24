package com.sharefable.api.config;

import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequest;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.get.GetSettingsResponse;
import org.elasticsearch.client.IndicesClient;
import org.elasticsearch.client.LicenseClient;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ESClientConfigTest {

    @Autowired
    RestHighLevelClient client;

    @Test
    void elasticsearchClient() throws IOException {
        IndicesClient indices = client.indices();
        GetSettingsRequest req = new GetSettingsRequest().indices("my-index-000004");
        GetSettingsResponse settings = indices.getSettings(req, RequestOptions.DEFAULT);
        System.out.println(">>> " + settings.toString());
    }
}
