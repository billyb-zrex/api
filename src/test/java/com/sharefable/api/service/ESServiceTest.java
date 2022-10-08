package com.sharefable.api.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.entity.AssetContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class ESServiceTest {
    @Autowired
    ESService service;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void reset() throws IOException, InterruptedException {
        service.deleteAllDocumentsFromIndex();
        Thread.sleep(3000);
    }

    private AssetContent getData(Map<String, String> params, String reqBodyStr) throws JsonProcessingException {
        return AssetContent.builder()
            .assetId(1L)
            .assetPath("/api/acme")
            .method("GET")
            .reqParams(mapper.valueToTree(params))
            .reqParamsStr(mapper.valueToTree(params).toString())
            .reqBody(mapper.readTree(reqBodyStr))
            .reqBodyStr(reqBodyStr)
            .reqHeaders(mapper.valueToTree(params).toString())
            .respHeaders(mapper.valueToTree(params).toString())
            .respDataUri("loc/in/s3")
            .build();
    }

    private AssetContent getDefaultData() throws JsonProcessingException {
        Map<String, String> params = new HashMap<>();
        params.put("from", "hn");

        String reqBodyStr = "{ \"key\": \"value\" }";
        return getData(params, reqBodyStr);
    }

    @Test
    void shouldBeAbleToIndexAndMatchASimpleDocument() throws JsonProcessingException, InterruptedException {
        AssetContent data = getDefaultData();

        Assertions.assertNull(data.getId());
        AssetContent updatedData = service.insertDocument(data);
        Assertions.assertNotNull(updatedData);
        Assertions.assertNotNull(updatedData.getId());

        // ES indexing is near realtime, hence we have to wait for sometime before the docs gets indexed.
        Thread.sleep(3000);

        AssetContent exactMatch = service.getExactDocument(data);
        Assertions.assertNotNull(exactMatch);
        Assertions.assertEquals(exactMatch.getId(), data.getId());

        Thread.sleep(500);

        AssetContent updatedDoc = AssetContent.builder().id(exactMatch.getId())
            .respDataUri("new" + java.time.Instant.now().toEpochMilli() + "/loc/in/s3")
            .build();
        AssetContent assetContent = service.updateDocument(updatedDoc);
        Assertions.assertEquals(assetContent.getRespDataUri(), updatedDoc.getRespDataUri());
    }

    @Test
    void shouldBeAbleToMatchWithOptionalParameter() throws JsonProcessingException, InterruptedException {
        AssetContent data = getDefaultData();

        data.setReqParams(null);
        data.setReqParamsStr(null);

        AssetContent storedData = service.insertDocument(data);
        Assertions.assertNotNull(storedData);
        Assertions.assertNotNull(storedData.getId());

        Thread.sleep(3000);

        AssetContent exactMatch = service.getExactDocument(data);
        Assertions.assertNotNull(exactMatch);
        Assertions.assertNotNull(exactMatch.getId());
    }

    @Test
    void shouldBeAbleToFindMatchesForADocument() throws JsonProcessingException, InterruptedException {
        Map<String, String> params = new HashMap<>();
        params.put("from", "gog");
        String reqBodyStr = "{ \"key1\": \"value1\" }";
        AssetContent data1 = getData(params, reqBodyStr);

        Map<String, String> params2 = new HashMap<>();
        params2.put("from", "hn");
        params2.put("rec", "today");
        String reqBodyStr2 = "{ \"key2\": \"value2\", \"key3\": \"value3\" }";
        AssetContent data2 = getData(params2, reqBodyStr2);

        service.insertDocument(data1);
        service.insertDocument(data2);

        Thread.sleep(3000);

        Map<String, String> params3 = new HashMap<>();
        params3.put("from", "hn");
        String reqBodyStr3 = "{\"key3\": \"value3\" }";
        AssetContent data3 = getData(params3, reqBodyStr3);

        List<AssetContent> matchedDocuments = service.getMatchedDocuments(data3);
        Assertions.assertTrue(matchedDocuments.size() >= 1);
        Assertions.assertNotNull(matchedDocuments.get(0).getId());
        Assertions.assertEquals(matchedDocuments.get(0).getReqBodyStr(), data2.getReqBodyStr());
        Assertions.assertEquals(matchedDocuments.get(0).getReqParamsStr(), data2.getReqParamsStr());
    }
}
