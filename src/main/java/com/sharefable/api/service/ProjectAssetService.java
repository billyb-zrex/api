package com.sharefable.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.common.UpdateLog;
import com.sharefable.api.common.Utils;
import com.sharefable.api.common.content.BaseAssetBodyParser;
import com.sharefable.api.common.content.ContentTypeParser;
import com.sharefable.api.common.content.FileNameResolver;
import com.sharefable.api.common.req.AssetContentBody;
import com.sharefable.api.common.req.NewProxyAssetReqBodyParsed;
import com.sharefable.api.common.req.QueryProxyAssetReqBody;
import com.sharefable.api.common.req.ReqParamMissingException;
import com.sharefable.api.common.resp.ProxyAssetMappingResp;
import com.sharefable.api.entity.AssetContent;
import com.sharefable.api.entity.AssetMapping;
import com.sharefable.api.entity.Project;
import com.sharefable.api.repo.ProjectRepo;
import com.sharefable.api.repo.ProxyAssetRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Streamable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class ProjectAssetService {
    ObjectMapper mapper = new ObjectMapper();

    private final ProjectRepo projectRepo;

    private final ProxyAssetRepo proxyAssetRepo;


    private final List<Project.FieldRef> updatableFields = Project.UPDATABLE_FIELDS;

    private final S3Service s3Service;

    private final ESService esService;

    @Autowired
    public ProjectAssetService(ProjectRepo projectRepo, ProxyAssetRepo proxyAssetRepo, S3Service s3Service, ESService esService) {
        this.projectRepo = projectRepo;
        this.proxyAssetRepo = proxyAssetRepo;
        this.s3Service = s3Service;
        this.esService = esService;
    }

    @Transactional
    public Project newProject(String displayName, String origin, String title) {
        String name = Utils.normalizeProjectName(displayName);

        Project project = Project.builder()
            .name(name)
            .displayName(displayName)
            .origin(origin)
            .title(title)
            .proxyOrigin("") // todo fix this
            .build();

        return projectRepo.save(project);
    }

    @Transactional(readOnly = true)
    public List<Project> getAllProjects() {
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
        if (projectWrap.isPresent()) {
            Project project = projectWrap.get();
            for (UpdateLog<Project.FieldRef> log : collection) {
                switch (log.getField()) {
                    case DisplayName:
                        project.setDisplayName((String) log.getValue());
                        break;

                    case Thumbnail:
                        project.setThumbnail((String) log.getValue());
                        break;

                    default:
                        break;
                }
            }
            return Optional.of(projectRepo.save(project));
        }
        return Optional.empty();
    }

    private AssetMapping findAssetMapping(Long projectId, String assetName, String assetPath) {
        List<AssetMapping> mappings = proxyAssetRepo.findAssetMappingByProjectIdAndAssetName(projectId, assetName);
        AssetMapping matchedMapping = null;
        for (AssetMapping mapping : mappings) {
            // For long assetPath (more than 255 chars) the assetName might be same
            if (mapping.getAssetPath().equals(assetPath)) {
                matchedMapping = mapping;
                break;
            }
        }
        return matchedMapping;
    }

    @Transactional
    public AssetMapping createAssetMapping(Long projectId, NewProxyAssetReqBodyParsed body) throws ReqParamMissingException {
        String assetPath = body.getUrl().getPath();
        String assetName = Utils.getAssetNameFromAssetPath(assetPath);
        AssetMapping matchedMapping = findAssetMapping(projectId, assetName, assetPath);

        boolean newMappingCreated = false;
        if (matchedMapping == null) {
            // If a mapping is not found in db, then create a new mapping

            newMappingCreated = true;
            matchedMapping = AssetMapping.builder()
                .projectId(projectId)
                .assetPath(assetPath)
                .assetName(assetName)
                .origin(body.getOrigin().getPath())
                .status(body.getStatus())
                .method(body.getMethod())
                .contentType(body.getContentType().toString())
                .meta(body.getMeta())
                .build();

            matchedMapping = proxyAssetRepo.save(matchedMapping);
        }

        // If status != 302 then there would always be response body
        // If status == 302 there won't be any response body
        String fileName = null;
        if (body.getStatus() != HttpStatus.FOUND) {
            BaseAssetBodyParser parser = ContentTypeParser.parse(body);
            fileName = parser.fileName();
            String fullQualifiedFileName = "project/" + projectId + "/" + fileName;
            s3Service.upload(fullQualifiedFileName, body.getContentType().getType(), parser.getContent());
        }

        Map<String, String> queryParams;
        if (body.getQueryParams() == null || body.getQueryParams().size() == 0) {
            queryParams = null;
        } else {
            queryParams = body.getQueryParams();
        }
        String queryParamsStr = null;
        if (queryParams != null) {
            queryParamsStr = mapper.valueToTree(queryParams).toString();
        }

        AssetContent asset = AssetContent.builder()
            .assetId(matchedMapping.getId())
            .assetPath(assetPath)
            .method(body.getMethod().toString())
            .reqParams(queryParams)
            .reqParamsStr(queryParamsStr)
            .reqBodyStr(body.getReqBody())
            .reqHeaders(mapper.valueToTree(body.getReqHeaders()).toString())
            .respHeaders(mapper.valueToTree(body.getRespHeaders()).toString())
            .respDataUri(fileName)
            .build();

        if (newMappingCreated) {
            // If mapping is not found in db, means there won't be an entry in elasticsearch,
            // so we directly insert the document in elasticsearch
            esService.insertDocument(asset);
        } else {
            // Check if the document in elasticsearch, if not then insert a new document, if exists then update
            // the response file uri
            AssetContent exactDocument = esService.getExactDocument(asset);
            if (exactDocument == null) {
                esService.insertDocument(asset);
            } else {
                AssetContent updateDoc = AssetContent.builder()
                    .id(exactDocument.getId())
                    .respDataUri(fileName)
                    .build();
                esService.updateDocument(updateDoc);
            }
        }

        return matchedMapping;
    }

    @Transactional(readOnly = true)
    public ProxyAssetMappingResp getAssetByName(AssetMapping reqAsset, QueryProxyAssetReqBody body) {
        String assetName = Utils.getAssetNameFromAssetPath(reqAsset.getAssetPath());
        AssetMapping asset = findAssetMapping(reqAsset.getProjectId(), assetName, reqAsset.getAssetPath());

        ProxyAssetMappingResp.ProxyAssetMappingRespBuilder proxyBuilder = ProxyAssetMappingResp.builder();
        if (asset == null) {
            log.warn("Request for asset mapping not found. ReqAsset {} body {}", reqAsset, body);
            return proxyBuilder.isFound(false).build();
        }

        AssetContent content = AssetContent.builder()
            .assetId(asset.getId())
            .assetPath(asset.getAssetPath())
            .method(asset.getMethod().toString())
            .reqParams(body.getQueryParams())
            .reqParamsStr(body.getQueryParams() == null ? null : mapper.valueToTree(body.getQueryParams()).toString())
            .reqBodyStr(body.getReqBody())
            .build();

        List<AssetContent> matchedDocuments = esService.getMatchedDocuments(content);
        if (matchedDocuments == null || matchedDocuments.size() == 0) {
            log.warn("Request for asset mapping is found but not matches of content. ReqAsset {} body {}", asset, body);
            return proxyBuilder.isFound(false).build();
        }

        proxyBuilder.isFound(true).proxy(asset).content(content);


        AssetContent target = matchedDocuments.get(0);
        String respLocation = target.getRespDataUri();
        byte[] bodyContent;
        if (!(respLocation == null || respLocation.equals(""))) {
            String fullQualifiedFileName = "project/" + reqAsset.getProjectId() + "/" + respLocation;
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
