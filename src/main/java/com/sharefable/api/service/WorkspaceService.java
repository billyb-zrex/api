package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.ImageType;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AssetPathConfig;
import com.sharefable.api.entity.Org;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.OrgRepo;
import com.sharefable.api.repo.UserRepo;
import com.sharefable.api.transport.NewOrgReq;
import com.sharefable.api.transport.NewUserReq;
import com.sharefable.api.transport.OrgResp;
import com.sharefable.api.transport.UserResp;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/*
 * TODO upon implementation of authentication check if the users have access to certain entity
 */

@Service
@Slf4j
public class WorkspaceService {
    private final OrgRepo orgRepo;
    private final S3Service s3Service;
    private final UserRepo userRepo;

    private final AssetPathConfig pathConfig;

    @Autowired
    public WorkspaceService(OrgRepo orgRepo, UserRepo userRepo, S3Service s3Service, AssetPathConfig pathConfig) {
        this.orgRepo = orgRepo;
        this.s3Service = s3Service;
        this.userRepo = userRepo;
        this.pathConfig = pathConfig;
    }

    @Transactional
    public OrgResp newOrg(NewOrgReq body) {
        String displayName = body.getDisplayName();
        String rid = Utils.createReadableId(displayName);

        Org.OrgBuilder orgBuilder = Org.builder().displayName(displayName).rid(rid);

        if (StringUtils.isNotBlank(body.getThumbnail())) {
            Pair<byte[], ImageType> imgDataAndType = Utils.getImageDataFromBase64Str(body.getThumbnail());
            if (imgDataAndType.getValue1() == ImageType.Unknown) {
                log.error("Can't find type from image data. Only allowed type is png. Skipping saving of image.");
            } else {
                String filePath = UUID.randomUUID() + "." + imgDataAndType.getValue1().type;
                AssetFilePath assetFilePath = pathConfig.getQualifiedPathFor(AssetPathConfig.AssetType.Common, filePath);
                s3Service.upload(assetFilePath, imgDataAndType.getValue0());
                orgBuilder.thumbnail(assetFilePath.getFilePath());
            }
        }
        Org org = orgBuilder.build();
        Org savedOrg = orgRepo.save(org);
        return OrgResp.from(savedOrg);
    }

    @Transactional(readOnly = true)
    public OrgResp getOrgById(Long id) {
        Optional<Org> org = orgRepo.findById(id);
        return org.map(OrgResp::from).orElse(OrgResp.Empty());
    }

    @Transactional
    public UserResp newUser(NewUserReq body) {
        User user = User.builder()
            .firstName(body.getFirstName())
            .lastName(body.getLastName())
            .email(body.getEmail())
            .avatar(body.getAvatar())
            .belongsToOrg(Org.builder().id(body.getBelongsToOrg()).build())
            .build();

        User savedUser = userRepo.save(user);
        return UserResp.from(savedUser);
    }

    @Transactional
    Optional<User> getUserEntity(Long id) {
        return userRepo.findById(id);
    }
}
