package com.sharefable.api.controller.v1;


import com.sharefable.api.common.ApiResp;
import com.sharefable.api.common.req.NewProxyAssetReqBody;
import com.sharefable.api.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.api.common.req.QueryProxyAssetReqBody;
import com.sharefable.api.common.req.ReqParamMissingException;
import com.sharefable.api.common.resp.ProxyAssetMappingResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.AssetMapping;
import com.sharefable.api.service.ProjectAssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

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
        AssetMapping assetMapping = projectAssetService.createAssetMapping(projectId, parsedBody);
        if (assetMapping == null) {
            return ApiResp.builder()
                .status(ApiResp.ResponseStatus.Failure)
                .errStr("Can't create mapping")
                .errCode(ApiResp.ErrorCode.NotFound).build();
        }
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data("ok").build();
    }

    @RequestMapping(
        value = Routes.GET_PROXY_ASSET,
        method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.PATCH }
    )
    public ResponseEntity<byte[]> getProxyAsset(
        @PathVariable("id") Long projectId,
        @PathVariable("proxy") String proxyPath,
        HttpServletRequest req) {
        log.info("Asset request uri: {}, method: {}", req.getRequestURI(), req.getMethod());

        proxyPath = UriUtils.encodePath(proxyPath, StandardCharsets.UTF_8);

        AssetMapping asset = AssetMapping.builder()
            .assetPath(proxyPath)
            .projectId(projectId)
            .build();

        try {
            Map<String, String> reqParams = null;
            String rawQueryStr = req.getQueryString();
            if (rawQueryStr != null && !rawQueryStr.trim().equalsIgnoreCase("")) {
                reqParams = UriComponentsBuilder
                    .fromUri(new URI("https://stash.sharefable.com?" + rawQueryStr))
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();
            }

            String reqBody = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
            QueryProxyAssetReqBody body = QueryProxyAssetReqBody.builder()
                .method(HttpMethod.resolve(req.getMethod()))
                .queryParams(reqParams)
                .reqBody(reqBody)
                .build();

            ProxyAssetMappingResp mapping = projectAssetService.getAssetByName(asset, body);

            if (!mapping.isFound()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new byte[]{});
            }

            String contentType = mapping.getProxy().getContentType();
            HttpHeaders headers = new HttpHeaders();
            if (contentType != null && !contentType.equals("")) {
                headers.add("Content-Type", contentType);
            }

            return ResponseEntity.status(mapping.getProxy().getStatus())
                .headers(headers)
                .body(mapping.getBody());

        } catch (MalformedURLException | URISyntaxException e) {
            log.error("Could not form url for searching. Error: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        } catch (IOException e) {
            log.error("Could not read request from body. Error: {}", e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
}
