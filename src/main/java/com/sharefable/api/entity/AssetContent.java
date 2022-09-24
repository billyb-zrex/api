package com.sharefable.api.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AssetContent {
    @Id
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
    private Object resp;
}
