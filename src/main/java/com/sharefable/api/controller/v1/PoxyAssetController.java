package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.ProxyAssetService;
import com.sharefable.api.transport.ParsedReqProxyAsset;
import com.sharefable.api.transport.ReqProxyAsset;
import com.sharefable.api.transport.RespProxyAsset;
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
    public ApiResp<RespProxyAsset> proxyAsset(@RequestBody ReqProxyAsset body) {
        Optional<ParsedReqProxyAsset> parsedBody = ParsedReqProxyAsset.from(body);
        if (parsedBody.isEmpty()) {
            return ApiResp.<RespProxyAsset>builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Could not create proxy asset").build();
        }

        RespProxyAsset proxyAsset = proxyAssetService.createProxyAsset(parsedBody.get());
        return ApiResp.<RespProxyAsset>builder().status(ApiResp.ResponseStatus.Success).data(proxyAsset).build();
    }
}
