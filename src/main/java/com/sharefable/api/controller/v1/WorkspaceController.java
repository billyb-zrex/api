package com.sharefable.api.controller.v1;

import com.sharefable.api.auth.AuthUser;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.User;
import com.sharefable.api.service.WorkspaceService;
import com.sharefable.api.transport.ObjectValidationResult;
import com.sharefable.api.transport.req.ReqNewOrg;
import com.sharefable.api.transport.req.ReqUpdateUser;
import com.sharefable.api.transport.resp.RespCommonConfig;
import com.sharefable.api.transport.resp.RespOrg;
import com.sharefable.api.transport.resp.RespUploadUrl;
import com.sharefable.api.transport.resp.RespUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@SuppressWarnings("removal")
@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
@RequiredArgsConstructor
public class WorkspaceController {
    private final WorkspaceService wsService;
    private final AppSettings settings;

    @RequestMapping(value = Routes.NEW_ORG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespOrg> createNewOrg(@RequestBody ReqNewOrg body, @AuthUser User user) {
        ObjectValidationResult validation = body.validate();
        if (!validation.isValid()) {
            String reasons = String.join("; ", validation.validationMsg());
            log.error("Could not create org, reason {}", reasons);
            return ApiResp.<RespOrg>builder()
                .status(ApiResp.ResponseStatus.Failure)
                .errCode(ApiResp.ErrorCode.IllegalArgs)
                .errStr(reasons)
                .build();
        }
        body = body.normalizeDisplayName();
        RespOrg org = wsService.createNewOrgAndAssignUserToIt(body, user);
        return ApiResp.<RespOrg>builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.GET_ORG_FOR_USER, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<List<RespOrg>> getOrgForUser(@AuthUser User user) {
        List<RespOrg> org = wsService.getOrgByEmail(user.getEmail());
        return ApiResp.<List<RespOrg>>builder().status(ApiResp.ResponseStatus.Success).data(org).build();
    }

    @RequestMapping(value = Routes.ASSIGN_IMPLICIT_USER_ORG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespUser> assignDefaultOrgForUserUsingDomain(@AuthUser User user) {
        RespUser updatedUser = wsService.assignUserToImplicitOrg(user);
        return ApiResp.<RespUser>builder().status(ApiResp.ResponseStatus.Success).data(updatedUser).build();
    }

    @RequestMapping(value = Routes.IAM, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespUser> newUser(@AuthUser User user) {
        RespUser respUser = wsService.getUserWithOrgData(user);
        return ApiResp.<RespUser>builder().status(ApiResp.ResponseStatus.Success).data(respUser).build();
    }

    @RequestMapping(value = Routes.UPDATE_USER_PROP, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespUser> updateUserName(@RequestBody ReqUpdateUser body, @AuthUser User user) {
        body = body.normalize();
        RespUser respUser = wsService.updateUserFirstAndLastName(body, user);
        return ApiResp.<RespUser>builder().status(ApiResp.ResponseStatus.Success).data(respUser).build();
    }

    @RequestMapping(value = Routes.GET_COMMON_CONFIG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespCommonConfig> getCommonConfig() {
        RespCommonConfig.RespCommonConfigBuilder builder = RespCommonConfig.builder();
        wsService.getCommonConfig(builder);
        builder.latestSchemaVersion(settings.currentSchemaVersion());
        RespCommonConfig resp = builder.build();
        return ApiResp.<RespCommonConfig>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
    }


    @PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
    @RequestMapping(value = Routes.UPLOAD_LINK, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespUploadUrl> getPresignedUrl(
        @RequestParam("te") String contentTypeEncoded,
        @RequestParam("ext") Optional<String> maybeExtension,
        @AuthUser User user) {
        String contentType = new String(org.springframework.util.Base64Utils.decodeFromString(contentTypeEncoded), StandardCharsets.UTF_8);
        RespUploadUrl resp = wsService.getPreSignedUrlToUploadFile(user, contentType, maybeExtension);
        return ApiResp.<RespUploadUrl>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
    }
}
