package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.WorkspaceService;
import com.sharefable.api.transport.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class WorkspaceController {
    private final WorkspaceService wsService;

    @Autowired
    public WorkspaceController(WorkspaceService wsService) {
        this.wsService = wsService;
    }

    @RequestMapping(value = Routes.NEW_ORG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespOrg> createNewOrg(@RequestBody ReqNewOrg body) {
        ObjectValidationResult validation = body.validate();
        if (!validation.isValid()) {
            return ApiResp.<RespOrg>builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr(String.join("; ", validation.validationMsg())).build();
        }
        body = body.normalizeDisplayName();
        RespOrg org = wsService.newOrg(body);
        return ApiResp.<RespOrg>builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.GET_ORG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespOrg> getOrg(@RequestParam("rid") Optional<String> rId) {
        if (rId.isEmpty()) {
            return ApiResp.<RespOrg>builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Missing parameter").build();
        }
        RespOrg org = wsService.getOrgByRId(rId.get());
        return ApiResp.<RespOrg>builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.NEW_USER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespUser> newUser(@RequestBody ReqNewUser body) {
        RespUser user = wsService.newUser(body);
        return ApiResp.<RespUser>builder().status(ApiResp.ResponseStatus.Success).data(user).build();
    }

    @RequestMapping(value = Routes.GET_USER, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespUser> getUser(@RequestParam Long id) {
        RespUser resp = wsService.getUserEntity(id);
        return ApiResp.<RespUser>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
    }

    @RequestMapping(value = Routes.GET_COMMON_CONFIG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespCommonConfig> getCommonConfig() {
        RespCommonConfig.RespCommonConfigBuilder builder = RespCommonConfig.builder();
        wsService.getCommonConfig(builder);
        RespCommonConfig resp = builder.build();
        return ApiResp.<RespCommonConfig>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
    }
}
