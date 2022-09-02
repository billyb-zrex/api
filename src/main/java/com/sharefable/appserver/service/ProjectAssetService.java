package com.sharefable.appserver.service;

import com.sharefable.appserver.common.UpdateLog;
import com.sharefable.appserver.common.Utils;
import com.sharefable.appserver.common.content.BaseAssetBodyParser;
import com.sharefable.appserver.common.content.ContentTypeParser;
import com.sharefable.appserver.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.appserver.common.req.ReqParamMissingException;
import com.sharefable.appserver.entity.AssetMapping;
import com.sharefable.appserver.entity.Project;
import com.sharefable.appserver.repo.ProxyAssetRepo;
import com.sharefable.appserver.repo.ProjectRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ProjectAssetService {
    private final ProjectRepo projectRepo;

    private final ProxyAssetRepo proxyAssetRepo;

    private final List<Project.FieldRef> updatableFields = Project.UPDATABLE_FIELDS;

    private final S3Service s3Service;

    @Autowired
    public ProjectAssetService(ProjectRepo projectRepo, ProxyAssetRepo proxyAssetRepo, S3Service s3Service) {
        this.projectRepo = projectRepo;
        this.proxyAssetRepo = proxyAssetRepo;
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
    public AssetMapping createAssetMapping(Long projectId, NewProxyAssetReqBodyParsed body) throws ReqParamMissingException {
        List<AssetMapping> activeMappings = proxyAssetRepo.findAssetMappingByProjectIdAndIsActiveIsTrue(projectId);
        for (AssetMapping asset : activeMappings) {
            if (Utils.isSavedAssetIsSameWithNewAsset(asset, body)) {
                // If there is a new asset incoming with same definition as of a one in storage, then we mark the prev
                // one as inactive and then mark the incoming one as active
                asset.setIsActive(false);
                proxyAssetRepo.save(asset);
                break;
            }
        }

        AssetMapping.AssetMappingBuilder mappingBuilder = AssetMapping.builder();
        mappingBuilder
            .projectId(projectId)
            .assetPath(body.getUrl().getPath())
            .origin(body.getOrigin().getPath())
            .status(body.getStatus())
            .method(body.getMethod())
            .reqHeaders(body.getReqHeaders())
            .respHeaders(body.getRespHeaders())
            .isActive(true)
            .contentType(body.getContentType().toString())
            .meta(body.getMeta());

        // If status != 302 then there would always be response body
        // If status == 302 there won't be any response body
        if (body.getStatus() != HttpStatus.MOVED_PERMANENTLY){
            BaseAssetBodyParser parser = ContentTypeParser.parse(body);
            String fileName = parser.fileName();
            String fullQualifiedFileName = "project/" + projectId + "/" + fileName;
            s3Service.upload(fullQualifiedFileName, body.getContentType().getType(), parser.getContent());
            mappingBuilder.location(fileName);
        }

        AssetMapping mapping = mappingBuilder.build();
        return proxyAssetRepo.save(mapping);
    }
}
