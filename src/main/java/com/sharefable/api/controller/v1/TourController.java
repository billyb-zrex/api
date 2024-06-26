package com.sharefable.api.controller.v1;

import com.sharefable.api.auth.AuthUser;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.ApiKey;
import com.sharefable.api.entity.User;
import com.sharefable.api.service.TourService;
import com.sharefable.api.service.WorkspaceService;
import com.sharefable.api.transport.EditTour;
import com.sharefable.api.transport.OnboardingTourForPrev;
import com.sharefable.api.transport.TourDeleted;
import com.sharefable.api.transport.req.*;
import com.sharefable.api.transport.resp.RespCommonConfig;
import com.sharefable.api.transport.resp.RespTour;
import com.sharefable.api.transport.resp.RespTourWithScreens;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
@RequiredArgsConstructor
public class TourController {
  private final TourService tourService;
  private final WorkspaceController wsController;
  private final WorkspaceService wsService;
  private final AppSettings appSettings;

  @RequestMapping(value = Routes.GET_ALL_TOURS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  //@PreAuthorize("hasAuthority(@Perm.READ_TOUR)")
  public ApiResp<RespTour[]> getAllTours(@AuthUser User user) {
    Long orgId = user.getBelongsToOrg();
    List<RespTour> allTours = tourService.getAllToursForOrg(orgId, TourDeleted.ACTIVE);
    return ApiResp.<RespTour[]>builder().status(ApiResp.ResponseStatus.Success).data(allTours.toArray(RespTour[]::new)).build();
  }

  @RequestMapping(value = Routes.NEW_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  //@PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
  public ApiResp<RespTour> newTour(@RequestBody ReqNewTour body, @AuthUser User user) {
    ReqNewTour req = body.normalizeDisplayName();
    RespTour tour = tourService.createNewTour(req, user);
    return ApiResp.<RespTour>builder().status(ApiResp.ResponseStatus.Success).data(tour).build();
  }

  @RequestMapping(value = Routes.GET_TOUR, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespTour> getTourByRId(@RequestParam("rid") String rId, @RequestParam("s") Optional<Boolean> shouldGetScreens, @RequestParam("_i") Optional<Boolean> shouldGetDeleted) {
    RespTour tour = tourService.getTourByRid(rId, shouldGetScreens.orElse(Boolean.FALSE), shouldGetDeleted.orElse(Boolean.FALSE));
    return ApiResp.<RespTour>builder().data(tour).build();
  }

  @RequestMapping(value = Routes.GET_TOUR_BY_ID, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespTour> getTourById(@PathVariable("id") Long id) {
    RespTour resp = tourService.getTourById(id);
    return ApiResp.<RespTour>builder().data(resp).build();
  }

  @RequestMapping(value = Routes.RECORD_TOUR_EDIT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  //@PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
  public ApiResp<RespTour> recordEdit(@RequestBody ReqRecordEdit body, @AuthUser User user) {
    RespTour resp = tourService.updateEditForTour(body, user, EditTour.INDEX);
    return ApiResp.<RespTour>builder().data(resp).build();
  }

  @RequestMapping(value = Routes.RECORD_TOUR_LOADER_EDIT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  //@PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
  public ApiResp<RespTour> recordLoaderEdit(@RequestBody ReqRecordEdit body, @AuthUser User user) {
    RespTour resp = tourService.updateEditForTour(body, user, EditTour.LOADER);
    return ApiResp.<RespTour>builder().data(resp).build();
  }

  @RequestMapping(value = Routes.RENAME_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  //@PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
  public ApiResp<RespTour> renameTour(@RequestBody ReqRenameGeneric body, @AuthUser User user) {
    ReqRenameGeneric nBody = body.normalizeDisplayName();
    RespTour resp = tourService.renameTour(nBody, user);
    return ApiResp.<RespTour>builder().data(resp).build();
  }

  @RequestMapping(value = Routes.DUPLICATE_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  //@PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
  @Transactional
  public ApiResp<RespTourWithScreens> duplicateTour(@RequestBody ReqDuplicateTour body, @AuthUser User user) {
    ReqDuplicateTour nBody = body.normalizeDisplayName();
    RespTourWithScreens resp = tourService.duplicateTour(nBody, user);
    return ApiResp.<RespTourWithScreens>builder().data(resp).build();
  }

  @RequestMapping(value = Routes.DELETE_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  //@PreAuthorize("hasAuthority(@Perm.WRITE_TOUR)")
  public ApiResp<RespTour[]> deleteTour(@RequestBody ReqTourRid body, @AuthUser User user) {
    List<RespTour> allTours = tourService.removeTour(body, user);
    return ApiResp.<RespTour[]>builder().status(ApiResp.ResponseStatus.Success).data(allTours.toArray(RespTour[]::new)).build();
  }

  @RequestMapping(value = Routes.PUBLISH_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespTour> publishTour(@RequestBody ReqTourRid body, @AuthUser User user) {
    RespCommonConfig commonConfig = wsController.getCommonConfig().getData();
    RespTour resp = tourService.publishTour(body, user, commonConfig);
    return ApiResp.<RespTour>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.PUBLISH_TOUR_INTERNAL, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespTour> publishTour(@RequestBody ReqTourRid body) {
    if (!appSettings.isMigrationFlatSet()) {
      log.error("Migration requested but flag not set.");
      throw new ResponseStatusException(HttpStatusCode.valueOf(404));
    }
    RespCommonConfig commonConfig = wsController.getCommonConfig().getData();
    RespTour resp = tourService.publishTour(body, commonConfig);
    return ApiResp.<RespTour>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.ONBOARDING_TOUR, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespTourWithScreens[]> getOnboardingTours(@AuthUser User user) {
    List<RespTourWithScreens> allOnboardingTours = tourService.createOnboardingTourInUserAccount(user);
    return ApiResp.<RespTourWithScreens[]>builder().status(ApiResp.ResponseStatus.Success).data(allOnboardingTours.toArray(RespTourWithScreens[]::new)).build();
  }

  @RequestMapping(value = Routes.ONBOARDING_TOUR_PREVIEW_ONLY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<OnboardingTourForPrev>> getOnboardingToursForPreview(@AuthUser User user) {
    List<OnboardingTourForPrev> tourForPreview = tourService.getOnboardingToursForPreview(user);
    return ApiResp.<List<OnboardingTourForPrev>>builder().status(ApiResp.ResponseStatus.Success).data(tourForPreview).build();
  }

  @RequestMapping(value = Routes.UPDATE_TOUR_PROPERTY, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespTour> updateTourProperty(@RequestBody ReqTourPropUpdate body, @AuthUser User user) {
    RespTour respTour = tourService.updateTourProperty(body, user);
    return ApiResp.<RespTour>builder().status(ApiResp.ResponseStatus.Success).data(respTour).build();
  }

  @RequestMapping(value = Routes.GET_TOUR_ASSET_FILE_PATH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<String> getTourAssetPath(@RequestParam("id") Long tourId) {
    String assetPath = tourService.getAssetPathForTour(tourId);
    return ApiResp.<String>builder().status(ApiResp.ResponseStatus.Success).data(assetPath).build();
  }

  @RequestMapping(value = Routes.GET_ALL_TOURS_BY_API_KEY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<RespTour>> getAllTours(@RequestHeader(name = "X-API-KEY") String apiKey) {
    ApiKey key = wsService.getApiKey(apiKey);
    log.info("GET_ALL_TOURS_API_KEY api key {}", apiKey);
    if (key == null) {
      log.error("Can't find api key {}", apiKey);
      throw new ResponseStatusException(HttpStatusCode.valueOf(404));
    }
    List<RespTour> allTours = tourService.getAllToursForOrg(key.getOrg().getId(), TourDeleted.ACTIVE);
    log.info("GET_ALL_TOURS_API_KEY  orgId {} len {}", key.getOrg().getId(), allTours.size());
    return ApiResp.<List<RespTour>>builder().status(ApiResp.ResponseStatus.Success).data(allTours).build();
  }

  @RequestMapping(value = Routes.COPY_TOUR_TO_DIFFERENT_ORG, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<RespTourWithScreens>> copyToursToDifferentOrg(@RequestBody ReqTransferTour body) {
    if (!appSettings.isMigrationFlatSet()) {
      log.error("Migration requested but flag not set.");
      throw new ResponseStatusException(HttpStatusCode.valueOf(404));
    }
    List<RespTourWithScreens> respTourWithScreens = tourService.copyToursToDifferentOrg(body);
    return ApiResp.<List<RespTourWithScreens>>builder().status(ApiResp.ResponseStatus.Success).data(respTourWithScreens).build();
  }
}
