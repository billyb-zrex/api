package com.sharefable.appserver.service;

import com.sharefable.appserver.common.UpdateLog;
import com.sharefable.appserver.common.Utils;
import com.sharefable.appserver.common.content.BaseParser;
import com.sharefable.appserver.common.content.ContentTypeParser;
import com.sharefable.appserver.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.appserver.common.req.ReqParamMissingException;
import com.sharefable.appserver.entity.Project;
import com.sharefable.appserver.repo.ProjectRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ProjectAssetService {
    private final ProjectRepo projectRepo;

    private final List<Project.FieldRef> updatableFields = Project.UPDATABLE_FIELDS;

    private final S3Service s3Service;

    @Autowired
    public ProjectAssetService(ProjectRepo projectRepo, S3Service s3Service) {
        this.projectRepo = projectRepo;
        this.s3Service = s3Service;
    }

    @Transactional
    public Project newProject(String displayName) {
        String name = Utils.normalizeProjectName(displayName);

        Project project = Project.builder()
            .name(name)
            .displayName(displayName)
            .build();

        return projectRepo.save(project);
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects(){
        return Streamable.of(projectRepo.findAll(Sort.by(Sort.Direction.DESC, "updatedAt"))).toList();
    }

    @Transactional
    public Optional<Project> updateProject(Long projectId,
                                           Map<UpdateLog.UpdateType, List<UpdateLog<Project.FieldRef>>> updateLogCollection) {
        List<UpdateLog<Project.FieldRef>> updateLogImgProp = updateLogCollection.get(UpdateLog.UpdateType.Img);
        // Upload the image content to s3 and replace the image content with filename
        List<UpdateLog<Project.FieldRef>> updateLogsWithImgLoc = new ArrayList<>();
        for (UpdateLog<Project.FieldRef> log : updateLogImgProp) {
            String randomFileName = UUID.randomUUID().toString();
            String qualifiedFileName = "project/" + projectId + "/" + randomFileName;
            s3Service.upload(qualifiedFileName, "img", (String) log.getValue());
            updateLogsWithImgLoc.add(UpdateLog.clone(log, randomFileName));
        }

        // Merge the Str update stream and image stream which now contains the filename uploaded to s3
        Stream<UpdateLog<Project.FieldRef>> changeLog =
            Stream.concat(updateLogCollection.get(UpdateLog.UpdateType.Str).stream(), updateLogsWithImgLoc.stream());
        List<UpdateLog<Project.FieldRef>> collection = changeLog.filter(log -> updatableFields.contains(log.getField()))
            .collect(Collectors.toList());

        // WARN Extra db call to find the entity. Fix this later. Make the update work with partially constructed
        //  entity i.e. null values for non-updatable field
        Optional<Project> projectWrap = projectRepo.findById(projectId);
        if (projectWrap.isPresent()){
            Project project = projectWrap.get();
            for (UpdateLog<Project.FieldRef> log : collection) {
                switch (log.getField()) {
                    case DisplayName:
                        project.setDisplayName((String)log.getValue());
                        break;

                    case Thumbnail:
                        project.setThumbnail((String)log.getValue());
                        break;

                    default:
                        break;
                }
            }
            return Optional.of(projectRepo.save(project));
        }
        return Optional.empty();
    }

    @Transactional
    public void createAssetMapping(Long projectId, NewProxyAssetReqBodyParsed body) throws ReqParamMissingException {
        BaseParser parse = ContentTypeParser.parse(body);
    }
}
