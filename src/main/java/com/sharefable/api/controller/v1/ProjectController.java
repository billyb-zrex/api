package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.common.UpdateLog;
import com.sharefable.api.common.req.NewProjectReqBody;
import com.sharefable.api.common.req.UpdateProjectReqBody;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.Project;
import com.sharefable.api.service.ProjectAssetService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        value = Routes.NEW_PROJECT,
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp createNewProject(@RequestBody NewProjectReqBody body) {
        log.info("{} called with body {}", Routes.NEW_PROJECT, body);

        String projectName = body.getName();
        if (projectName == null || projectName.trim().equals("")) {
            return ApiResp.builder()
                .status(ApiResp.ResponseStatus.Failure)
                .errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr("Project name can't be empty")
                .build();
        }

        Project savedProject = projectAssetService.newProject(projectName, body.getOrigin(), body.getTitle());
        return ApiResp.builder()
            .status(ApiResp.ResponseStatus.Success)
            .data(savedProject)
            .build();
    }

    @RequestMapping(
        value = Routes.GET_ALL_PROJECTS,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp getAllProjects() {
        log.info("{} called", Routes.GET_ALL_PROJECTS);
        List<Project> allProjects = projectAssetService.getAllProjects();
        return ApiResp.builder()
            .status(ApiResp.ResponseStatus.Success)
            .data(allProjects)
            .build();
    }

    @RequestMapping(
        value = Routes.GET_PROJECT,
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ApiResp getProject(@PathVariable("id") Long projectId) {
        log.info("{} called with id={}", Routes.GET_PROJECT, projectId);
        Project project = projectAssetService.getProjectById(projectId);
        return ApiResp.builder()
            .status(ApiResp.ResponseStatus.Success)
            .data(project)
            .build();
    }


    @RequestMapping(
        value = Routes.UPDATE_PROJECT,
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp updateProject(@PathVariable("id") Long projectId, @RequestBody UpdateProjectReqBody body) {
        log.info("{} called with id {} and body {}", Routes.UPDATE_PROJECT, projectId, body);

        Map<UpdateLog.UpdateType, List<UpdateLog<Project.FieldRef>>> updateLogCollection =
            body.getChange().stream()
                // Resolve string field name to proper field name from enum
                .map(log -> UpdateLog.clone(log, Project.FieldRef::valueOf))
                .collect(Collectors.groupingBy(UpdateLog::getType));

        Optional<Project> updateLogStrProp = projectAssetService.updateProject(projectId, updateLogCollection);

        if (updateLogStrProp.isPresent()) {
            return ApiResp.builder()
                .status(ApiResp.ResponseStatus.Success)
                .data(updateLogStrProp.get())
                .build();
        }

        log.warn("Project with id {} not found", projectId);
        return ApiResp.builder()
            .status(ApiResp.ResponseStatus.Success)
            .data(Project.builder().build())
            .build();
    }
}
