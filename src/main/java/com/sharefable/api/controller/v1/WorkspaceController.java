package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.WorkspaceService;
import com.sharefable.api.transport.NewOrgReq;
import com.sharefable.api.transport.NewUserReq;
import com.sharefable.api.transport.OrgResp;
import com.sharefable.api.transport.UserResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
    public ApiResp createNewOrg(@RequestBody NewOrgReq body) {
        String name = body.getDisplayName();
        if (StringUtils.isBlank(name)) {
            log.error("Org name should not be blank");
            return ApiResp.builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Org name should not be blank").build();
        }
        body.setDisplayName(body.getDisplayName().trim());
        OrgResp org = wsService.newOrg(body);
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.GET_ORG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp getOrg(@RequestParam("id") Optional<Long> id) {
        if (!id.isPresent()) {
            return ApiResp.builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Missing parameter").build();
        }
        OrgResp org = wsService.getOrgById(id.get());
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.NEW_USER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp newUser(@RequestBody NewUserReq body) {
        UserResp user = wsService.newUser(body);
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(user).build();
    }
}
