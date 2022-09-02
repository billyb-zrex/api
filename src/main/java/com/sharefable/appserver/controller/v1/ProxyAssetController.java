package com.sharefable.appserver.controller.v1;


import com.sharefable.appserver.common.ApiResp;
import com.sharefable.appserver.common.req.NewProxyAssetReqBody;
import com.sharefable.appserver.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.appserver.common.req.ReqParamMissingException;
import com.sharefable.appserver.controller.Routes;
import com.sharefable.appserver.service.ProjectAssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;

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
}
