package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.Org;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.OrgRepo;
import com.sharefable.api.repo.UserRepo;
import com.sharefable.api.transport.*;
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
    private final S3Config s3Config;

    @Autowired
    public WorkspaceService(OrgRepo orgRepo, UserRepo userRepo, S3Service s3Service, S3Config s3Config) {
        super(s3Service, s3Config);
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.s3Config = s3Config;
    }

    @Transactional
    public RespOrg newOrg(ReqNewOrg body) {
        String displayName = body.displayName();
        String rid = Utils.createReadableId(displayName);

        Org.OrgBuilder orgBuilder = Org.builder().displayName(displayName).rid(rid);
        if (StringUtils.isNotBlank(body.thumbnail())) {
            Optional<AssetFilePath> assetFilePath = uploadBase64ImageToS3(body.thumbnail(), S3Config.AssetType.Common);
            assetFilePath.ifPresent(filePath -> orgBuilder.thumbnail(filePath.getFilePath()));
        }
        Org org = orgBuilder.build();
        Org savedOrg = orgRepo.save(org);
        return RespOrg.from(savedOrg);
    }

    @Transactional(readOnly = true)
    public RespOrg getOrgByRId(String id) {
        Optional<Org> org = orgRepo.findFirstByRid(id);
        return org.map(RespOrg::from).orElse(RespOrg.Empty());
    }

    @Transactional
    public RespUser newUser(ReqNewUser body) {
        User user = User.builder()
            .firstName(body.firstName())
            .lastName(body.lastName())
            .email(body.email())
            .avatar(body.avatar())
            .belongsToOrg(Org.builder().id(body.belongsToOrg()).build())
            .build();

        User savedUser = userRepo.save(user);
        return RespUser.from(savedUser);
    }

    @Transactional
    public RespUser getUserEntity(Long id) {
        Optional<User> user = userRepo.findById(id);
        return user.map(RespUser::from).orElse(RespUser.Empty());
    }

    public void getCommonConfig(RespCommonConfig.RespCommonConfigBuilder builder) {
        S3Config.PathConfigForClient pathConfig = s3Config.getPathConfigForClient();
        S3Config.FileNames fileNames = s3Config.getFileNames();
        builder
            .commonAssetPath(pathConfig.commonAsset())
            .screenAssetPath(pathConfig.screenAsset())
            .tourAssetPath(pathConfig.tourAsset())
            .dataFileName(fileNames.dataFile());
    }
}
