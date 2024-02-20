package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.Org;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.OrgRepo;
import com.sharefable.api.repo.ScreenRepo;
import com.sharefable.api.repo.TourRepo;
import com.sharefable.api.repo.UserRepo;
import com.sharefable.api.transport.req.ReqNewOrg;
import com.sharefable.api.transport.req.ReqUpdateUser;
import com.sharefable.api.transport.resp.RespCommonConfig;
import com.sharefable.api.transport.resp.RespOrg;
import com.sharefable.api.transport.resp.RespUploadUrl;
import com.sharefable.api.transport.resp.RespUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WorkspaceService extends ServiceBase {
    private final OrgRepo orgRepo;
    private final S3Config s3Config;
    private final UserRepo userRepo;
    private final UserService userService;
    private final S3Service s3Service;

    @Autowired
    public WorkspaceService(OrgRepo orgRepo, UserRepo userRepo, S3Service s3Service, S3Config s3Config, AppSettings settings, ScreenRepo screenRepo, TourRepo tourRepo, UserService userService) {
        super(settings, s3Service, s3Config, screenRepo, tourRepo);
        this.orgRepo = orgRepo;
        this.userRepo = userRepo;
        this.s3Config = s3Config;
        this.s3Service = s3Service;
        this.userService = userService;
    }

    @Transactional
    public RespOrg createNewOrgAndAssignUserToIt(ReqNewOrg body, User user) {
        Pair<String, Boolean> domainInf = Utils.getDomainFromEmailForRespectiveEmail(user.getEmail());
        String emailDomain = domainInf.getValue0();

        // For now only one org per domain is allowed
        Set<Org> orgs = orgRepo.findOrgByDomain(emailDomain);
        if (!orgs.isEmpty()) {
            log.error("Org for domain is already present but still requested by {}", user);
            return RespOrg.from(orgs.iterator().next());
        }

        String displayName = body.displayName();
        String rid = Utils.createReadableId(displayName);
        Org.OrgBuilder orgBuilder = Org.builder()
            .displayName(displayName)
            .domain(emailDomain)
            .rid(rid);
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
    public RespOrg getOrgForUser(User user) {
        if (user.getBelongsToOrg() == null) return RespOrg.Empty();
        Optional<Org> org = orgRepo.findById(user.getBelongsToOrg());
        return org.map(RespOrg::from).orElse(RespOrg.Empty());
    }

    @Transactional(readOnly = true)
    public RespOrg getOrgByEmail(String email) {
        Pair<String, Boolean> domainInf = Utils.getDomainFromEmailForRespectiveEmail(email);
        String emailDomain = domainInf.getValue0();
        Set<Org> org = orgRepo.findOrgByDomain(emailDomain);
        return org.isEmpty() ? RespOrg.Empty() : RespOrg.from(org.iterator().next());

    }

    @Transactional
    public RespUser assignUserToImplicitOrg(User user) {
        Pair<String, Boolean> domainInf = Utils.getDomainFromEmailForRespectiveEmail(user.getEmail());
        String emailDomain = domainInf.getValue0();
        Set<Org> orgs = orgRepo.findOrgByDomain(emailDomain);
        if (!orgs.isEmpty()) {
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
            Pair<String, Boolean> domainInf = Utils.getDomainFromEmailForRespectiveEmail(user.getEmail());
            String emailDomain = domainInf.getValue0();
            // If user is not part of an org then find out is there implicit org that is present as part of user's
            // email domain
            Set<Org> orgs = orgRepo.findOrgByDomain(emailDomain);
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
            .pubTourAssetPath(pathConfig.tourPublishedAsset())
            .dataFileName(entityFilesConfig.dataFile().filename())
            .loaderFileName(entityFilesConfig.loaderFile().filename())
            .editFileName(entityFilesConfig.editFile().filename())
            .pubDataFileName(entityFilesConfig.publishedDataFile().filename())
            .pubLoaderFileName(entityFilesConfig.publishedLoaderFile().filename())
            .pubEditFileName(entityFilesConfig.publishedEditFile().filename())
            .pubTourEntityFileName(entityFilesConfig.publishedTourEntityFile().filename())
            .manifestFileName(entityFilesConfig.manifestFile().filename());
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

    public List<RespUser> getAllUsersInOrg(Long orgId) {
        Set<User> users = userRepo.getUsersByBelongsToOrgAndActiveIsTrue(orgId);
        Set<User> inactiveUsers = userRepo.getUsersByBelongsToOrgAndActiveIsFalse(orgId);
        users.addAll(inactiveUsers);

        return users.stream().map(RespUser::from).collect(Collectors.toList());
    }

    public RespUser activateOrDeactivateUser(Long targetUserId, Boolean activate, User reqByUser) {
        Optional<User> maybeUser = userRepo.findById(targetUserId);
        if (maybeUser.isEmpty()) return null;
        User targetUser = maybeUser.get();
        if (!Objects.equals(targetUser.getBelongsToOrg(), reqByUser.getBelongsToOrg()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Users not from same org");

        User changedUser = userService.setUserActiveOrInactive(targetUser, activate);
        return RespUser.from(changedUser);
    }
}
