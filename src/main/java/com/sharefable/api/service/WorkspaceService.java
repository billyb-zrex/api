package com.sharefable.api.service;

import com.sharefable.api.common.ImageType;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.Org;
import com.sharefable.api.repo.OrgRepo;
import com.sharefable.api.transport.NewOrgReqBody;
import com.sharefable.api.transport.NewOrgResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class WorkspaceService {
    private final OrgRepo orgRepo;
    private final S3Service s3Service;

    @Autowired
    public WorkspaceService(OrgRepo orgRepo, S3Service s3Service) {
        this.orgRepo = orgRepo;
        this.s3Service = s3Service;
    }

    @Transactional
    public NewOrgResp newOrg(NewOrgReqBody body) {
        String displayName = body.getDisplayName();
        String rid = Utils.createReadableId(displayName);

        Org.OrgBuilder orgBuilder = Org.builder().displayName(displayName).rid(rid);

        if (StringUtils.isNotBlank(body.getThumbnail())) {
            Pair<byte[], ImageType> imgDataAndType = Utils.getImageDataFromBase64Str(body.getThumbnail());
            if (imgDataAndType.getValue1() == ImageType.Unknown) {
                log.error("Can't find type from image data. Only allowed type is png. Skipping saving of image.");
            } else {
                String filePath = UUID.randomUUID() + "." + imgDataAndType.getValue1().type;
                s3Service.upload(filePath, S3Service.AssetType.AppGeneric, imgDataAndType.getValue0());
                orgBuilder.thumbnail(filePath);
            }
        }
        Org org = orgBuilder.build();
        Org savedOrg = orgRepo.save(org);
        return NewOrgResp.from(savedOrg);
    }

    @Transactional(readOnly = true)
    public NewOrgResp getOrgById(Long id) {
        // TODO check if user has access to get the org
        Optional<Org> org = orgRepo.findById(id);
        return org.map(NewOrgResp::from).orElse(NewOrgResp.Empty());
    }
}
