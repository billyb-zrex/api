package com.sharefable.appserver.controller.v1;


import com.sharefable.appserver.common.ApiResp;
import com.sharefable.appserver.common.req.NewProxyAssetReqBody;
import com.sharefable.appserver.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.appserver.common.req.ReqParamMissingException;
import com.sharefable.appserver.common.resp.ProxyAssetMappingResp;
import com.sharefable.appserver.controller.Routes;
import com.sharefable.appserver.entity.AssetMapping;
import com.sharefable.appserver.service.ProjectAssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
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
        value = Routes.GET_ASSET,
        method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.DELETE, RequestMethod.PUT, RequestMethod.PATCH }
    )
    public ResponseEntity<byte[]> getProxyAsset(@PathVariable("id") Long projectId, @PathVariable("proxy") String proxyPath, HttpServletRequest req) {
        ProxyAssetMappingResp mapping = projectAssetService.getAssetByName(
            projectId,
            proxyPath,
            HttpMethod.resolve(req.getMethod()),
            req.getQueryString()
        );

        if (!mapping.isFound()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new byte[]{});
        }

        HttpHeaders headers = new HttpHeaders();
        AssetMapping proxy = mapping.getProxy();
        Map<String, String> respHeaders = proxy.getRespHeaders();
        if (respHeaders != null) {
            for (String key : respHeaders.keySet()) {
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
