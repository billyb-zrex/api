package com.sharefable.api.common.req;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class QueryProxyAssetReqBody {
    private Map<String, String> queryParams;
    private String reqBody;
    private HttpMethod method;
}
