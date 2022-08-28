package com.sharefable.appserver.controller.v1;

import com.sharefable.appserver.common.ApiResp;
import com.sharefable.appserver.common.UpdateLog;
import com.sharefable.appserver.common.req.NewProjectReqBody;
import com.sharefable.appserver.common.req.UpdateProjectReqBody;
import com.sharefable.appserver.controller.Routes;
import com.sharefable.appserver.entity.Project;
import com.sharefable.appserver.service.ProjectAssetService;
import com.sharefable.appserver.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class ProjectController {
    private final ProjectAssetService projectAssetService;
    private final S3Service s3Service;

    @Autowired
    public ProjectController(ProjectAssetService projectAssetService, S3Service s3Service) {
        this.projectAssetService = projectAssetService;
        this.s3Service = s3Service;
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

        Project savedProject = projectAssetService.newProject(projectName);
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
        value = Routes.UPDATE_PROJECT,
        method = RequestMethod.POST,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp updateProject(@PathVariable("id") Long projectId, @RequestBody UpdateProjectReqBody body) {
        log.info("{} called with id {} and body {}", Routes.UPDATE_PROJECT, projectId, body);

        Map<UpdateLog.UpdateType, List<UpdateLog<Project.FieldRef>>> collection =
            body.getChange().stream()
                // Resolve string field name to proper field name from enum
                .map(log -> UpdateLog.clone(log, Project.FieldRef::valueOf))
                .collect(Collectors.groupingBy(UpdateLog::getType));

        List<UpdateLog<Project.FieldRef>> updateLogImgProp = collection.get(UpdateLog.UpdateType.Img);

        // Upload the image content to s3 and replace the image content with filename
        List<UpdateLog<Project.FieldRef>> updateLogsWithImgLoc = new ArrayList<>();
        for (UpdateLog<Project.FieldRef> log : updateLogImgProp) {
            String randomFileName = UUID.randomUUID().toString();
            String qualifiedFileName = "project/" + projectId + "/" + randomFileName;
            s3Service.upload(qualifiedFileName, "img", (String) log.getValue());
            updateLogsWithImgLoc.add(UpdateLog.clone(log, randomFileName));
        }

        Optional<Project> updateLogStrProp = projectAssetService.updateProject(
            projectId,
            Stream.concat(collection.get(UpdateLog.UpdateType.Str).stream(), updateLogsWithImgLoc.stream())
        );

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
