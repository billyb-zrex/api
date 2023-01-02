package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/*
 * TODO upon implementation of authentication check if the users have access to certain entity
 */

@Service
@Slf4j
public class WorkspaceService extends ServiceBase {
    private final OrgRepo orgRepo;
    private final UserRepo userRepo;

    @Autowired
    public WorkspaceService(OrgRepo orgRepo, UserRepo userRepo, S3Service s3Service, S3Config s3Config) {
        super(s3Service, s3Config);
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
    }

    @Transactional
    public OrgResp newOrg(NewOrgReq body) {
        String displayName = body.displayName();
        String rid = Utils.createReadableId(displayName);

        Org.OrgBuilder orgBuilder = Org.builder().displayName(displayName).rid(rid);
        if (StringUtils.isNotBlank(body.thumbnail())) {
            Optional<AssetFilePath> assetFilePath = uploadBase64ImageToS3(body.thumbnail(), S3Config.AssetType.Common);
            assetFilePath.ifPresent(filePath -> orgBuilder.thumbnail(filePath.getFilePath()));
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
            .firstName(body.firstName())
            .lastName(body.lastName())
            .email(body.email())
            .avatar(body.avatar())
            .belongsToOrg(Org.builder().id(body.belongsToOrg()).build())
            .build();

        User savedUser = userRepo.save(user);
        return UserResp.from(savedUser);
    }

    @Transactional
    Optional<User> getUserEntity(Long id) {
        return userRepo.findById(id);
    }
}
