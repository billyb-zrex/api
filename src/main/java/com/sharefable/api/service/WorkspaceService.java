package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppSettings;
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

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkspaceService extends ServiceBase {
    private final OrgRepo orgRepo;
    private final S3Config s3Config;
    private final UserRepo userRepo;
    private final S3Service s3Service;

    @Autowired
    public WorkspaceService(OrgRepo orgRepo, UserRepo userRepo, S3Service s3Service, S3Config s3Config, AppSettings settings) {
        super(settings, s3Service, s3Config);
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.s3Config = s3Config;
        this.s3Service = s3Service;
    }

    @Transactional
    public RespOrg createNewOrgAndAssignUserToIt(ReqNewOrg body, User user) {
        String emailDomain = Utils.getDomainFromEmail(user.getEmail());

        // For now only one org per domain is allowed
        Set<Org> orgs = orgRepo.findOrgByDomain(emailDomain);
        if (orgs.size() > 0) {
            log.error("Org for domain is already present but still requested by {}", user);
            return RespOrg.from(orgs.iterator().next());
        }

        String displayName = body.displayName();
        String rid = Utils.createReadableId(displayName);
        Org.OrgBuilder orgBuilder = Org.builder().displayName(displayName).domain(emailDomain).rid(rid);
        if (StringUtils.isNotBlank(body.thumbnail())) {
            Optional<AssetFilePath> assetFilePath = uploadBase64ImageToS3(body.thumbnail(), S3Config.AssetType.Common);
            assetFilePath.ifPresent(filePath -> orgBuilder.thumbnail(filePath.getFilePath()));
        }
        Org org = orgBuilder.build();
        Org savedOrg = orgRepo.save(org);
        user.setBelongsToOrg(savedOrg.getId());
        userRepo.save(user);
        return RespOrg.from(savedOrg);
    }

    /*
     * TODO
     *  When a property of an entity object gets updated, the normal standard is to pass an array of following struct
     *  [{ prop: 'firstname', val: 'John' },
     *  { prop: 'lastname', val: 'Doe' }]
     *  this format is not implement here
     */
    @Transactional
    public RespUser updateUserFirstAndLastName(ReqUpdateUser body, User user) {
        user.setFirstName(body.firstName());
        user.setLastName(body.lastName());
        User savedUser = userRepo.save(user);
        return RespUser.from(savedUser);
    }

    @Transactional(readOnly = true)
    public RespOrg getOrgByRId(String id) {
        Optional<Org> org = orgRepo.findFirstByRid(id);
        return org.map(RespOrg::from).orElse(RespOrg.Empty());
    }

    @Transactional(readOnly = true)
    public List<RespOrg> getOrgByEmail(String email) {
        String emailDomain = Utils.getDomainFromEmail(email);
        Set<Org> org = orgRepo.findOrgByDomain(emailDomain);
        return org.stream().map(RespOrg::from).collect(Collectors.toList());
    }

    @Transactional
    public RespUser assignUserToImplicitOrg(User user) {
        String emailDomain = Utils.getDomainFromEmail(user.getEmail());
        Set<Org> orgs = orgRepo.findOrgByDomain(emailDomain);
        if (orgs.size() > 0) {
            Org org = orgs.iterator().next();
            user.setBelongsToOrg(org.getId());
            userRepo.save(user);
        } else {
            log.error("No org present but call to assignUserToImplicitOrg is done by user {}", user);
        }
        return RespUser.from(user);
    }

    @Transactional(readOnly = true)
    public RespUser getUserWithOrgData(User user) {
        RespUser respUser = RespUser.from(user);
        // No DB operation should happen if the user is part of an org already.
        if (user.getBelongsToOrg() == null) {
            // If user is not part of an org then find out is there implicit org that is present as part of user's
            // email domain
            Set<Org> orgs = orgRepo.findOrgByDomain(Utils.getDomainFromEmail(user.getEmail()));
            respUser.setOrgAssociation(!orgs.isEmpty()
                ? RespUser.UserOrgAssociation.Implicit
                : RespUser.UserOrgAssociation.NA);
        } else {
            respUser.setOrgAssociation(RespUser.UserOrgAssociation.Explicit);
        }
        return respUser;
    }

    public void getCommonConfig(RespCommonConfig.RespCommonConfigBuilder builder) {
        S3Config.PathConfigForClient pathConfig = s3Config.getPathConfigForClient();
        S3Config.EntityFilesConfig entityFilesConfig = S3Config.getEntityFiles();
        builder
            .commonAssetPath(pathConfig.commonAsset())
            .screenAssetPath(pathConfig.screenAsset())
            .tourAssetPath(pathConfig.tourAsset())
            .dataFileName(entityFilesConfig.dataFile().filename())
            .editFileName(entityFilesConfig.editFile().filename());
    }

    public RespUploadUrl getPreSignedUrlToUploadFile(User user, String contentType, Optional<String> extension) {
        String filename = Utils.createUuidWord();
        if (extension.isPresent()) {
            filename += StringUtils.prependIfMissing(extension.get(), ".");
        }
        AssetFilePath filePath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.UserGenerated, user.getBelongsToOrg().toString(), filename);
        URL url = s3Service.preSignedUrl(filePath, contentType);
        log.warn("content type {} url {}", contentType, url);
        return RespUploadUrl.builder()
            .url(url.toString())
            .expiry("default")
            .filename(filename)
            .build();
    }

}
