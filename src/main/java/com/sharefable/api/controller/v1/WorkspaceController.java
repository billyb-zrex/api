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
    public ApiResp<ResponseBase> createNewOrg(@RequestBody ReqNewOrg body) {
        ObjectValidationResult validation = body.validate();
        if (!validation.isValid()) {
            return ApiResp.builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr(String.join("; ", validation.validationMsg())).build();
        }
        body = body.normalizeDisplayName();
        RespOrg org = wsService.newOrg(body);
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.GET_ORG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<ResponseBase> getOrg(@RequestParam("rid") Optional<String> rId) {
        if (rId.isEmpty()) {
            return ApiResp.builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Missing parameter").build();
        }
        RespOrg org = wsService.getOrgByRId(rId.get());
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.NEW_USER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<ResponseBase> newUser(@RequestBody ReqNewUser body) {
        RespUser user = wsService.newUser(body);
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(user).build();
    }

    @RequestMapping(value = Routes.GET_USER, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<ResponseBase> getUser(@RequestParam Long id) {
        RespUser resp = wsService.getUserEntity(id);
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
    }
}
