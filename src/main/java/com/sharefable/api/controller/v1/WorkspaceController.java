package com.sharefable.api.controller.v1;

import com.sharefable.api.auth.AuthUser;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.User;
import com.sharefable.api.service.WorkspaceService;
import com.sharefable.api.transport.ObjectValidationResult;
import com.sharefable.api.transport.req.*;
import com.sharefable.api.transport.resp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

  @RequestMapping(value = Routes.ASSIGN_IMPLICIT_USER_ORG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespUser> assignDefaultOrgForUserUsingDomain(@AuthUser User user) {
    log.warn("[deprecate] #assignUserToImplicitOrg after v1.2.41");
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
    builder.latestSchemaVersion(settings.getCurrentSchemaVersion());
    RespCommonConfig resp = builder.build();
    return ApiResp.<RespCommonConfig>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.REFRESH_SETTINGS, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<String> refreshSettings() {
    settings.load();
    return ApiResp.<String>builder().status(ApiResp.ResponseStatus.Success).data("ok").build();
  }

  //@PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
  @RequestMapping(value = Routes.UPLOAD_LINK, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespUploadUrl> getPresignedUrl(
    @RequestParam("te") String contentTypeEncoded,
    @RequestParam("ext") Optional<String> maybeExtension,
    @AuthUser User user) {
    String contentType = new String(org.springframework.util.Base64Utils.decodeFromString(contentTypeEncoded), StandardCharsets.UTF_8);
    RespUploadUrl resp = wsService.getPreSignedUrlToUploadFile(user, contentType, maybeExtension);
    return ApiResp.<RespUploadUrl>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.GET_ORG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespOrg> updateUserName(@RequestParam("if") Integer implicitFetch, @AuthUser User user) {
    RespOrg org = implicitFetch == 1 ? wsService.getOrgByEmail(user.getEmail()) : wsService.getOrgForUser(user);
    return ApiResp.<RespOrg>builder().status(ApiResp.ResponseStatus.Success).data(org).build();
  }

  @RequestMapping(value = Routes.GET_ALL_USER_IN_ORG, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespUser[]> updateUserName(@AuthUser User user) {
    List<RespUser> users = wsService.getAllUsersInAnOrg(user.getBelongsToOrg());
    return ApiResp.<RespUser[]>builder().status(ApiResp.ResponseStatus.Success).data(users.toArray(RespUser[]::new)).build();
  }

  @RequestMapping(value = Routes.ACTIVATE_OR_DEACTIVATE_USER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespUser> updateUserName(@RequestBody ReqActivateOrDeactivateUser body, @AuthUser User user) {
    RespUser changedUser = wsService.activateOrDeactivateUser(body.userId(), body.shouldActivate(), user);
    return ApiResp.<RespUser>builder().status(ApiResp.ResponseStatus.Success).data(changedUser).build();
  }

  @RequestMapping(value = Routes.CREATE_NEW_API_KEY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespApiKey> createNewApiKey(@AuthUser User user) {
    RespApiKey newApiKey = wsService.createNewApiKey(user);
    return ApiResp.<RespApiKey>builder().status(ApiResp.ResponseStatus.Success).data(newApiKey).build();
  }

  @RequestMapping(value = Routes.API_KEY_WEBHOOK_PROBE, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<String> probeApiKey(@RequestHeader("X-API-KEY") String apiKey) {
    String[] apiKeySplit = StringUtils.split(apiKey, ":");
    String postfix = "";
    if (apiKeySplit.length > 1) {
      postfix = "@" + apiKeySplit[0];
    }
    return ApiResp.<String>builder().status(ApiResp.ResponseStatus.Success).data("ok" + postfix).build();
  }

  @RequestMapping(value = Routes.GET_API_KEY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespApiKey> getApiKey(@AuthUser User user) {
    RespApiKey newApiKey = wsService.getActiveApiKeysForOrg(user.getBelongsToOrg());
    return ApiResp.<RespApiKey>builder().status(ApiResp.ResponseStatus.Success).data(newApiKey).build();
  }

  @RequestMapping(value = Routes.UPDATE_ORG_PROPS, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespOrg> updateOrgProps(@RequestBody ReqUpdateOrg updateOrg, @AuthUser User user) {
    RespOrg respOrg = wsService.updateOrgInfo(updateOrg, user);
    return ApiResp.<RespOrg>builder().status(ApiResp.ResponseStatus.Success).data(respOrg).build();
  }

  @RequestMapping(value = Routes.FEATURE_PLAN_MATRIX, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<Map<String, Object>> getFeaturePlanMatrix() {
    Map<String, Object> resp = settings.getFeaturePlanMatrix() == null ? new HashMap<>() : settings.getFeaturePlanMatrix();
    return ApiResp.<Map<String, Object>>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.NEW_INVITE, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespNewInvite> createNewInvite(@RequestBody ReqNewInvite body, @AuthUser User user) {
    RespNewInvite respInvite = wsService.createNewInvite(body, user);
    return ApiResp.<RespNewInvite>builder().status(ApiResp.ResponseStatus.Success).data(respInvite).build();
  }

  @RequestMapping(value = Routes.ALL_ORG_FOR_USER, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<RespOrg>> getAllOrgForUser(@AuthUser User user) {
    List<RespOrg> allOrgForUser = wsService.getAllOrgForUser(user);
    return ApiResp.<List<RespOrg>>builder().status(ApiResp.ResponseStatus.Success).data(allOrgForUser).build();
  }

  @RequestMapping(value = Routes.ASSIGN_ORG_TO_USER, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespOrg> assignOrgToUser(@RequestBody ReqAssignOrgToUser body, @AuthUser User user) {
    Pair<RespUser, RespOrg> pair = wsService.assignOrgToUser(body, user);
    return ApiResp.<RespOrg>builder().status(ApiResp.ResponseStatus.Success).data(pair.getValue1()).build();
  }

  @RequestMapping(value = Routes.GET_ALL_VANITY_DOMAINS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<RespVanityDomain>> getAllVanityDomains(@AuthUser User user) {
    List<RespVanityDomain> allVanityDomains = wsService.getAllVanityDomains(user.getBelongsToOrg());
    return ApiResp.<List<RespVanityDomain>>builder().status(ApiResp.ResponseStatus.Success).data(allVanityDomains).build();
  }

  @RequestMapping(value = Routes.ADD_NEW_VANITY_DOMAIN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespVanityDomain> createNewVanityDomain(@RequestBody ReqCreateOrDeleteNewVanityDomain req, @AuthUser User user) {
    RespVanityDomain resp = wsService.addNewVanityDomain(req.getDomainName(), user.getBelongsToOrg(), user);
    return ApiResp.<RespVanityDomain>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.DEL_VANITY_DOMAIN, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<RespVanityDomain>> deleteNewVanityDomains(@RequestBody ReqCreateOrDeleteNewVanityDomain req, @AuthUser User user) {
    List<RespVanityDomain> resp = wsService.deleteVanityDomain(req.getDomainName(), user.getBelongsToOrg(), user);
    return ApiResp.<List<RespVanityDomain>>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }
}
