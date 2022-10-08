package com.sharefable.api.common.req;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class NewProxyAssetReqBody {
    @JsonProperty("contentType")
    private String contentTypeNotParsed;

    private HttpMethod method;

    @JsonProperty("origin")
    private String originNotParsed;

    @JsonProperty("status")
    private int statusNotParsed;

    @JsonProperty("url")
    private String urlNotParsed;

    private boolean isNewVersion = false;

    private Map<String, String> reqHeaders;

    private Map<String, String> respHeaders;

    private String reqBody;

    private AssetContentBody respBody;
}
