package com.sharefable.api.controller.v1;


import com.sharefable.api.common.ApiResp;
import com.sharefable.api.common.req.NewProxyAssetReqBody;
import com.sharefable.api.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.api.common.req.ReqParamMissingException;
import com.sharefable.api.common.resp.ProxyAssetMappingResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.AssetMapping;
import com.sharefable.api.service.ProjectAssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class ProxyAssetController {
    private final ProjectAssetService projectAssetService;

    @Autowired
    public ProxyAssetController(ProjectAssetService projectAssetService) {
        this.projectAssetService = projectAssetService;
    }

    @RequestMapping(
        value = Routes.NEW_ASSET,
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp createOrUpdateProxyAssetMapping(
        @PathVariable("id") Long projectId,
        @RequestBody NewProxyAssetReqBody body) throws ReqParamMissingException, MalformedURLException {
        log.info("{} is called with projectId {} and body {}", Routes.NEW_ASSET, projectId, body);

        NewProxyAssetReqBodyParsed parsedBody = NewProxyAssetReqBodyParsed.from(body);
        projectAssetService.createAssetMapping(projectId, parsedBody);
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data("ok").build();
    }

    @RequestMapping(
        value = Routes.GET_PROXY_ASSET,
        method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.PATCH }
    )
    public ResponseEntity<byte[]> getProxyAsset(@PathVariable("id") Long projectId, @PathVariable("proxy") String proxyPath, HttpServletRequest req) {
        log.info("Asset request uri: {}, method: {}", req.getRequestURI(), req.getMethod());
        proxyPath = UriUtils.encodePath(proxyPath, StandardCharsets.UTF_8);
        ProxyAssetMappingResp mapping = projectAssetService.getAssetByName(
            projectId,
            proxyPath,
            HttpMethod.resolve(req.getMethod()),
            req.getQueryString()
        );

        if (!mapping.isFound()) {
            log.info("Proxy path {} is not found", proxyPath);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new byte[]{});
        }

        HttpHeaders headers = new HttpHeaders();
        AssetMapping proxy = mapping.getProxy();
        Map<String, String> respHeaders = proxy.getRespHeaders();
        if (respHeaders != null) {
            for (String key : respHeaders.keySet()) {
                if (key.equalsIgnoreCase("Transfer-Encoding")
                    || key.equalsIgnoreCase("Content-Encoding")){
                    // https://en.wikipedia.org/wiki/Chunked_transfer_encoding#:~:text=Chunked%20transfer%20encoding%20is%20a,received%20independently%20of%20one%20another.
                    // Since we load the data in memory and then serve it, this header is misleading
                    continue;
                }
                headers.add(key, respHeaders.get(key));
            }
        }

        String contentType = proxy.getContentType();
        if (contentType != null && !contentType.equals("")) {
            headers.add("Content-Type", contentType);
        }

        return ResponseEntity.status(proxy.getStatus())
            .headers(headers)
            .body(mapping.getBody());
    }

}
