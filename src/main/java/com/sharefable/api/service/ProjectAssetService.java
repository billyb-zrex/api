package com.sharefable.api.service;

import com.sharefable.api.common.UpdateLog;
import com.sharefable.api.common.Utils;
import com.sharefable.api.common.content.BaseAssetBodyParser;
import com.sharefable.api.common.content.ContentTypeParser;
import com.sharefable.api.common.content.FileNameResolver;
import com.sharefable.api.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.api.common.req.AssetContentBody;
import com.sharefable.api.common.req.ReqParamMissingException;
import com.sharefable.api.common.resp.ProxyAssetMappingResp;
import com.sharefable.api.entity.AssetMapping;
import com.sharefable.api.entity.Project;
import com.sharefable.api.repo.AssetContentRepo;
import com.sharefable.api.repo.ProxyAssetRepo;
import com.sharefable.api.repo.ProjectRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ProjectAssetService {
    private final ProjectRepo projectRepo;

    private final ProxyAssetRepo proxyAssetRepo;

    private final AssetContentRepo assetContentRepo;

    private final List<Project.FieldRef> updatableFields = Project.UPDATABLE_FIELDS;

    private final S3Service s3Service;

    @Autowired
    public ProjectAssetService(ProjectRepo projectRepo, ProxyAssetRepo proxyAssetRepo, AssetContentRepo assetContentRepo, S3Service s3Service) {
        this.projectRepo = projectRepo;
        this.proxyAssetRepo = proxyAssetRepo;
        this.s3Service = s3Service;
        this.assetContentRepo = assetContentRepo;
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
            BaseAssetBodyParser imgParser = ContentTypeParser.parse(new MediaType("image/webp"), new FileNameResolver() {
                @Override
                protected String generateFileName() {
                    return qualifiedFileName;
                }
            }, new AssetContentBody(true, log.getValue()));
            s3Service.upload(qualifiedFileName, "img", imgParser.getContent());
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

        String assetPath = body.getUrl().getPath();
        AssetMapping.AssetMappingBuilder mappingBuilder = AssetMapping.builder();
        mappingBuilder
            .projectId(projectId)
            .assetPath(assetPath)
            .assetName(Utils.getAssetNameFromAssetPath(assetPath))
            .origin(body.getOrigin().getPath())
            .status(body.getStatus())
            .method(body.getMethod())
            .reqHeaders(body.getReqHeaders())
            .respHeaders(body.getRespHeaders())
            .queryParams(body.getQueryParams())
            .isActive(true)
            .contentType(body.getContentType().toString())
            .meta(body.getMeta());

        // If status != 302 then there would always be response body
        // If status == 302 there won't be any response body
        if (body.getStatus() != HttpStatus.FOUND){
            BaseAssetBodyParser parser = ContentTypeParser.parse(body);
            String fileName = parser.fileName();
            String fullQualifiedFileName = "project/" + projectId + "/" + fileName;
            s3Service.upload(fullQualifiedFileName, body.getContentType().getType(), parser.getContent());
            mappingBuilder.location(fileName);
        }

        AssetMapping mapping = mappingBuilder.build();
        return proxyAssetRepo.save(mapping);
    }

    @Transactional(readOnly = true)
    public ProxyAssetMappingResp getAssetByName(Long projectId, String assetPath, HttpMethod method, String queryString) {
        String assetName = Utils.getAssetNameFromAssetPath(assetPath);
        List<AssetMapping> assets =
            proxyAssetRepo.findAssetMappingByProjectIdAndAssetNameAndIsActiveIsTrueOrderByUpdatedAtDesc(
                projectId,
                assetName
            );
        assets = assets.stream()
            .filter(asset -> asset.getMethod() == method)
            .filter(asset -> asset.getAssetPath().equals(assetPath))
            .collect(Collectors.toList());

        ProxyAssetMappingResp.ProxyAssetMappingRespBuilder proxyBuilder = ProxyAssetMappingResp.builder();
        if (assets.size() == 0)  {
            proxyBuilder.isFound(false);
            return proxyBuilder.build();
        } else {
            proxyBuilder.isFound(true);
        }

        Map<String, String> queryParams = new HashMap<>();
        if (queryString != null && !queryString.trim().equals("")) {
            try {
                queryParams = UriComponentsBuilder
                    .fromUri(new URI("https://stash.sharefable.com?" + queryString))
                    .build()
                    .getQueryParams()
                    .toSingleValueMap();
            } catch (URISyntaxException e) {
                log.warn("Can't extract query string for request matching. Error: {}", e.getMessage());
                e.printStackTrace();
            }
        }

        AssetMapping matchedAsset;
        if (queryParams.size() == 0) {
            // If no query parameter is passed then return the latest asset
            matchedAsset = assets.get(0);
        } else {
            List<Map<String, String>> allQueryParams =
                assets.stream().map(AssetMapping::getQueryParams).collect(Collectors.toList());

            int nearestMapIndex = Utils.getNearestMap(allQueryParams, queryParams);
            matchedAsset = assets.get(nearestMapIndex);
        }
        proxyBuilder.proxy(matchedAsset);

        String respLocation = matchedAsset.getLocation();
        byte[] bodyContent;
        if (!(respLocation == null || respLocation.equals(""))) {
            String fullQualifiedFileName = "project/" + projectId + "/" + respLocation;
            try {
                bodyContent = s3Service.getObjectContent(fullQualifiedFileName);
                proxyBuilder.body(bodyContent);
            } catch (IOException e) {
                log.error("Error while reading file from s3. Error: {}", e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        return proxyBuilder.build();
    }
}
