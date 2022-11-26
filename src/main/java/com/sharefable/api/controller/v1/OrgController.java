package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.WorkspaceService;
import com.sharefable.api.transport.NewOrgReqBody;
import com.sharefable.api.transport.NewOrgResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class OrgController {
    private final WorkspaceService wsService;

    @Autowired
    public OrgController(WorkspaceService wsService) {
        this.wsService = wsService;
    }

    @RequestMapping(value = Routes.NEW_ORG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp createNewProject(@RequestBody NewOrgReqBody body) {
        String name = body.getDisplayName();
        if (StringUtils.isBlank(name)) {
            log.error("Org name should not be blank");
            return ApiResp.builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Org name should not be blank").build();
        }
        body.setDisplayName(body.getDisplayName().trim());
        NewOrgResp org = wsService.newOrg(body);
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.GET_ORG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp getProject(@RequestParam("id") Optional<Long> id) {
        if (!id.isPresent()) {
            return ApiResp.builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Missing parameter").build();
        }
        NewOrgResp org = wsService.getOrgById(id.get());
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }
}
