package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.ProxyAssetService;
import com.sharefable.api.transport.ProxyAssetReq;
import com.sharefable.api.transport.ProxyAssetReqParsed;
import com.sharefable.api.transport.ProxyAssetResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class PoxyAssetController {
    private final ProxyAssetService proxyAssetService;

    @Autowired
    public PoxyAssetController(ProxyAssetService proxyAssetService) {
        this.proxyAssetService = proxyAssetService;
    }


    @RequestMapping(value = Routes.PROXY_ASSET, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp createNewOrg(@RequestBody ProxyAssetReq body) {
        Optional<ProxyAssetReqParsed> parsedBody = ProxyAssetReqParsed.from(body);
        if (!parsedBody.isPresent()) {
            return ApiResp.builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Could not create proxy asset").build();
        }

        ProxyAssetResp proxyAsset = proxyAssetService.createProxyAsset(parsedBody.get());
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(proxyAsset).build();
    }
}
