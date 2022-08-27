package com.sharefable.appserver.controller.v1;

import com.sharefable.appserver.common.ApiResp;
import com.sharefable.appserver.common.req.NewProjectReqBody;
import com.sharefable.appserver.controller.Routes;
import com.sharefable.appserver.entity.Project;
import com.sharefable.appserver.service.ProjectAssetService;
import lombok.extern.slf4j.Slf4j;
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
    private final ProjectAssetService projectAssetService;

    @Autowired
    public ProjectController(ProjectAssetService projectAssetService) {
        this.projectAssetService = projectAssetService;
    }

    @RequestMapping(
        value = Routes.PROJECT_NEW,
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp createNewProject(@RequestBody NewProjectReqBody body) {
        log.info("{} called with body {}", Routes.PROJECT_NEW, body);

        String projectName = body.getName();
        if (projectName.trim().equals("")) {
            return ApiResp.builder()
                .status(ApiResp.ResponseStatus.Failure)
                .errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Project name can't be empty")
                .build();
        }

        Project savedProject = projectAssetService.newProject(projectName);
        return ApiResp.builder()
            .status(ApiResp.ResponseStatus.Success)
            .data(savedProject)
            .build();
    }
}
