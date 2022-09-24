package com.sharefable.api.entity;

import com.sharefable.api.common.ESIndices;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

/*
 * Any changes to this requires changes to the index creation process
 * Index creation details -> settings/es.http
 */

@Document(indexName = ESIndices.AssetContent, createIndex = false)
@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssetContent {
    @Id
    @Field(type= FieldType.Keyword)
    private String id;

    @Field(type= FieldType.Keyword)
    private String assetId;

    @Field(type = FieldType.Keyword)
    private String assetPath;

    @Field(type = FieldType.Keyword)
    private String method;

    @Field(type = FieldType.Keyword)
    private String version;

    @Field(type=FieldType.Flattened)
    private Map<String, String> reqParams;

    @Field(type=FieldType.Flattened)
    private Object reqBody;

    @Field(type = FieldType.Text)
    String reqBodyStr;

    @Field(index = false)
    private Map<String, String> reqHeaders;

    @Field(index = false)
    private Map<String, String> respHeaders;

    @Field(index = false)
    private Object respData;
}
