package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.ProjectService;
import com.sharefable.api.transport.NewProjectReq;
import com.sharefable.api.transport.ProjectResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class ProjectController {
    private final ProjectService projectService;

    @Autowired
    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    @RequestMapping(value = Routes.NEW_PROJECT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp createNewOrg(@RequestBody NewProjectReq body) {
        String name = body.getDisplayName();
        if (StringUtils.isBlank(name)) {
            log.error("Project name should not be blank");
            return ApiResp.builder().status(ApiResp.ResponseStatus.Failure).errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Project name should not be blank").build();
        }
        body.setDisplayName(body.getDisplayName().trim());
        ProjectResp projectResp = projectService.newProject(body);
        return ApiResp.builder().status(ApiResp.ResponseStatus.Success).data(projectResp).build();
    }
}
