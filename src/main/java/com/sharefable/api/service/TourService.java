package com.sharefable.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.FnTourBuilder;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppConfig;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.Screen;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.ScreenRepo;
import com.sharefable.api.repo.TourRepo;
import com.sharefable.api.transport.*;
import com.sharefable.api.transport.req.*;
import com.sharefable.api.transport.resp.RespCommonConfig;
import com.sharefable.api.transport.resp.RespTour;
import com.sharefable.api.transport.resp.RespTourWithScreens;
import io.sentry.Sentry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TourService extends ServiceBase {
  private final static ObjectMapper objectMapper = new ObjectMapper();
  private final static String ONBOARDING_ERR_NO_TOUR_IDS = "This error is occurring because there may be no onboarding tour ids present in db while trying to create onboarding tours for an organisation";
  private final TourRepo tourRepo;
  private final S3Config s3Config;
  private final ScreenService screenService;
  private final S3Service s3Service;
  private final AppConfig appConfig;
  private final AppSettings settings;
  private final ScreenRepo screenRepo;

  @Autowired
  public TourService(
    TourRepo tourRepo,
    AppSettings settings,
    S3Service s3Service,
    S3Config s3Config,
    ScreenRepo screenRepo,
    ScreenService screenService,
    AppConfig appConfig) {
    super(settings, s3Service, s3Config, screenRepo, tourRepo);
    this.tourRepo = tourRepo;
    this.s3Config = s3Config;
    this.screenService = screenService;
    this.s3Service = s3Service;
    this.appConfig = appConfig;
    this.settings = settings;
    this.screenRepo = screenRepo;
  }

  @Transactional
  public List<RespTour> getAllToursForOrg(Long orgId) {
    List<Tour> tours = tourRepo.findAllByBelongsToOrgOrderByUpdatedAtDesc(orgId);
    return tours.stream().map(RespTour::from).collect(Collectors.toList());
  }

  @Transactional
  public RespTour createNewTour(ReqNewTour req, User createdByUser) {
    String prefixHash = Utils.createUuidWord();
    uploadTemplateFileToS3(prefixHash, DATA_FILE_TYPE.TOUR_INDEX);
    uploadTemplateFileToS3(prefixHash, DATA_FILE_TYPE.TOUR_LOADER);

    Tour tour = Tour.builder()
      .createdBy(createdByUser)
      .displayName(req.name())
      .description(req.description().orElse(""))
      .rid(Utils.createReadableId(req.name()))
      .inProgress(false)
      .publishedVersion(0)
      .assetPrefixHash(prefixHash)
      .belongsToOrg(createdByUser.getBelongsToOrg())
      .onboarding(false)
      .build();

    Tour storedTour = tourRepo.save(tour);
    return RespTour.from(storedTour);
  }

  @Transactional(readOnly = true)
  public RespTour getTourByRid(String rid, boolean shouldGetScreens) {
    Optional<Tour> maybeTour = tourRepo.findByRid(rid);
    if (maybeTour.isEmpty()) {
      log.error("Can't get tour by rid {}", rid);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not found");
    }
    if (shouldGetScreens) {
      Tour tour = maybeTour.get();
      Set<Screen> screens = tour.getScreens();
      tour.setScreens(screens);
      return RespTourWithScreens.from(tour);
    }
    return RespTour.from(maybeTour.get());
  }

  @Transactional
  public RespTour updateEditForTour(ReqRecordEdit body, User userEntity, EditTour fileTobeEdited) {
    Tour tour = getEntityByRIdWithAuthValidation(Tour.class, body.rid(), userEntity);

    uploadDataFileToS3(
      body.editData(),
      tour.getAssetPrefixHash(),
      fileTobeEdited == EditTour.INDEX ? S3Config.getEntityFiles().tourDataFile() : S3Config.getEntityFiles().loaderFile(),
      S3Config.AssetType.Tour);

    tour.setUpdatedAt(Utils.getCurrentUtcTimestamp());
    Tour updatedTour = tourRepo.save(tour);
    return RespTour.from(updatedTour);
  }

  @Transactional
  public RespTour renameTour(ReqRenameGeneric body, User userEntity) {
    Tour tour = getEntityByRIdWithAuthValidation(Tour.class, body.rid(), userEntity);
    String oldRid = tour.getRid();
    String newName = body.newName();
    tour.setDisplayName(newName);
    tour.setDescription(body.description().isPresent() ? body.description().get() : tour.getDescription());
    tour.setRid(Utils.createReadableId(newName));

    try {
      Tour updatedTour = tourRepo.save(tour);
      if (tour.getLastPublishedDate() != null) {
        uploadTourManifestToS3(updatedTour);
        modifyPublishedTourEntityPath(oldRid, tour.getRid());
      }
      return RespTour.from(updatedTour);
    } catch (Exception e) {
      log.error("Error while trying to publish tour", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while trying to rename the tour");
    }
  }

  private void modifyPublishedTourEntityPath(String oldRid, String newRid) {
    AssetFilePath fromPubTourEntityFile = s3Config.getQualifiedPathFor(
      S3Config.AssetType.PublishedTour,
      oldRid,
      S3Config.getEntityFiles().publishedTourEntityFile().filename());
    AssetFilePath toPubTourEntityFile = s3Config.getQualifiedPathFor(
      S3Config.AssetType.PublishedTour,
      newRid,
      S3Config.getEntityFiles().publishedTourEntityFile().filename());
    s3Service.copy(fromPubTourEntityFile, toPubTourEntityFile);
  }

  @Transactional
  public RespTourWithScreens duplicateTour(ReqDuplicateTour body, User user) {
    Tour fromTour = getEntityByRIdWithAuthValidation(Tour.class, body.fromTourRid(), user);
    return this.duplicateTour(fromTour, user, tour -> tour.onboarding(false).displayName(body.duplicateTourName()).description(""), false);
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public RespTourWithScreens duplicateTour(Tour fromTour, User user, FnTourBuilder f, boolean shouldCloneParentScreens) {
    AssetFilePath fromTourDataFilePath = s3Config.getQualifiedPathFor(
      S3Config.AssetType.Tour,
      fromTour.getAssetPrefixHash(),
      S3Config.getEntityFiles().tourDataFile().filename());
    AssetFilePath fromTourLoaderFilePath = s3Config.getQualifiedPathFor(
      S3Config.AssetType.Tour,
      fromTour.getAssetPrefixHash(),
      S3Config.getEntityFiles().loaderFile().filename());

    String prefixHash = Utils.createUuidWord();
    copyDataFileToS3(fromTourDataFilePath, prefixHash, DATA_FILE_TYPE.TOUR_INDEX);
    copyDataFileToS3(fromTourLoaderFilePath, prefixHash, DATA_FILE_TYPE.TOUR_LOADER);

    String rid = Utils.createReadableId(fromTour.getDisplayName()); // TODO rid would be different
    Tour.TourBuilder<?, ?> tourBuilder = Tour.builder()
      .assetPrefixHash(prefixHash)
      .belongsToOrg(fromTour.getBelongsToOrg())
      .rid(rid)
      .inProgress(true)
      .publishedVersion(0)
      .displayName(fromTour.getDisplayName())
      .description(fromTour.getDescription())
      .onboarding(fromTour.getOnboarding())
      .createdBy(user);
    tourBuilder = f.apply(tourBuilder);
    Tour tour = tourBuilder.build();
    Tour savedTour = tourRepo.save(tour);

    Set<Screen> sourceScreens = fromTour.getScreens();

    Map<Long, Long> oldAndNewParentScreenMap = new HashMap<>();
    if (shouldCloneParentScreens) {
      Set<Long> parentScreenIds = sourceScreens.stream().map(Screen::getParentScreenId).collect(Collectors.toSet());
      List<Screen> parentScreens = screenRepo.findAllByIdIn(parentScreenIds);
      for (Screen parentScreen : parentScreens) {
        Screen clonedParentScreen = screenService.cloneScreen(newParentScreen -> newParentScreen.tours(Set.of()).parentScreenId(0L), parentScreen, user, savedTour, tour.getBelongsToOrg());
        oldAndNewParentScreenMap.put(parentScreen.getId(), clonedParentScreen.getId());
      }
    }

    Set<Screen> clonedScreens = new HashSet<>(sourceScreens.size());
    Map<String, String> sourceAndClonedScreenIdMap = new HashMap<>(sourceScreens.size());
    for (Screen sourceScreen : sourceScreens) {
      Screen clonedScreen = screenService.cloneScreen(newSourceScreen ->
        newSourceScreen.parentScreenId(
          sourceScreen.getType() != ScreenType.SerDom ? 0L : shouldCloneParentScreens ? oldAndNewParentScreenMap.get(sourceScreen.getParentScreenId())
            : sourceScreen.getParentScreenId()), sourceScreen, user, savedTour, tour.getBelongsToOrg());
      clonedScreens.add(clonedScreen);
      sourceAndClonedScreenIdMap.put(Long.toString(sourceScreen.getId()), Long.toString(clonedScreen.getId()));
    }
    Tour.TourBuilder<?, ?> updatedTourBuilder = savedTour.toBuilder().screens(clonedScreens);
    Tour updatedTour = updatedTourBuilder.build();
    RespTourWithScreens resp = RespTourWithScreens.from(updatedTour);
    resp.setIdxm(Optional.of(sourceAndClonedScreenIdMap));


    return resp;

        /* =============================================================================================================
         * Tour duplication is a complex process and currently client has to play a role in it (for faster development)
         * 1. Server clones(duplicate) all the screens + Copy edit files of screens
         * 2. Create a duplicate tour
         * 3. Copy tour data (index.json) file for duplicated tour
         *
         * Now this should have been enough, but like things in reality, things are always more elaborate than it looks.
         * Tour data file contains screen id inside the json file for annotation map / hotspot etc. After creating
         * the duplicate all the screenId has been changed for the duplicated tour.
         *
         * Hence, we send the new tour information alongside, the screenId map that the client would use to change the
         * tour index file and make an edit record request. The reason we are not doing it in server is, server is very
         * transparent about the tour index data file, it has no information about it's construct etc.
         *
         * Client understand the construct hence it's easier for client to do this.
         *
         * The following code tried to do high level mutation to the tour data file. But not understanding construct
         * of tour data file made it difficult for the server to complete all the mutations.
         * =============================================================================================================

        try {
            String tourFileContent = new String(s3Service.getObjectContent(toTourFilePath), StandardCharsets.UTF_8);
            JsonNode rootNode = mapper.readTree(tourFileContent);
            JsonNode rootNodeCloned = rootNode.deepCopy();

            String v = rootNode.get("v").asText();
            if (StringUtils.equalsIgnoreCase(v, SchemaVersion.V1.toValue())) {
                JsonNode entities = rootNode.get("entities");

                Iterator<String> keyIterator = entities.fieldNames();
                while (keyIterator.hasNext()) {
                    String screenId = keyIterator.next();
                    String newScreenId = sourceAndClonedScreenIdMap.get(screenId);
                    ((ObjectNode) rootNodeCloned.get("entities")).remove(screenId);
                    ((ObjectNode) rootNodeCloned.get("entities")).set(newScreenId, entities.get(screenId));
                }

                s3Service.upload(
                    toTourFilePath,
                    rootNodeCloned.toString().getBytes(StandardCharsets.UTF_8),
                    Map.of()
                );
            } else {
                throw new IllegalStateException(String.format("Schema version %s not yet supported", v));
            }

        } catch (IOException | IllegalStateException e) {
            log.error("Error while reading duplicated tour's index file. Error {}", e.getMessage());
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "");
        }
        */
  }

  @Transactional
  public List<RespTour> removeTour(ReqTourRid body, User userEntity) {
    Tour tour = getEntityByRIdWithAuthValidation(Tour.class, body.tourRid(), userEntity);
    tourRepo.delete(tour);
    return getAllToursForOrg(userEntity.getBelongsToOrg());
  }


  @Transactional
  public RespTour publishTour(ReqTourRid body, User userEntity, RespCommonConfig commonConfig) {
    Tour tour = getEntityByRIdWithAuthValidation(Tour.class, body.tourRid(), userEntity);
    return copyDataForPublishTour(tour, commonConfig);
  }

  @Transactional
  public RespTour publishTour(ReqTourRid body, RespCommonConfig commonConfig) {
    Optional<Tour> maybeTour = tourRepo.findByRid(body.tourRid());
    if (maybeTour.isEmpty()) throw new RuntimeException("tour not present");
    return copyDataForPublishTour(maybeTour.get(), commonConfig);
  }

  @Transactional
  public RespTour copyDataForPublishTour(Tour tour, RespCommonConfig commonConfig) {
    Set<Screen> screens = tour.getScreens();

    try {
      uploadTourManifestToS3(tour);
      AssetFilePath fromTourDataFilePath = s3Config.getQualifiedPathFor(
        S3Config.AssetType.Tour,
        tour.getAssetPrefixHash(),
        S3Config.getEntityFiles().tourDataFile().filename());
      AssetFilePath fromTourLoaderFilePath = s3Config.getQualifiedPathFor(
        S3Config.AssetType.Tour,
        tour.getAssetPrefixHash(),
        S3Config.getEntityFiles().loaderFile().filename());

      Integer nextVersion = tour.getPublishedVersion() + 1;
      AssetFilePath toTourDataFilePath = s3Config.getQualifiedPathFor(
        S3Config.AssetType.Tour, tour.getAssetPrefixHash(), S3Config.getEntityFiles().publishedDataFile().filename(nextVersion));
      AssetFilePath toTourLoaderFilePath = s3Config.getQualifiedPathFor(
        S3Config.AssetType.Tour, tour.getAssetPrefixHash(), S3Config.getEntityFiles().publishedLoaderFile().filename(nextVersion));

      List<Callable<AssetFilePath>> tourInfoCopier = new ArrayList<>();
      Callable<AssetFilePath> tourDataCopier = () -> s3Service.copy(fromTourDataFilePath, toTourDataFilePath, Map.of(
        HttpHeaders.CONTENT_TYPE, "application/json",
        HttpHeaders.CACHE_CONTROL, S3Config.getCachePolicyStr(S3Config.getEntityFiles().publishedDataFile().cachePolicy())
      ));
      Callable<AssetFilePath> tourLoaderCopier = () -> s3Service.copy(fromTourLoaderFilePath, toTourLoaderFilePath, Map.of(
        HttpHeaders.CONTENT_TYPE, "application/json",
        HttpHeaders.CACHE_CONTROL, S3Config.getCachePolicyStr(S3Config.getEntityFiles().publishedLoaderFile().cachePolicy())
      ));

      tourInfoCopier.add(tourDataCopier);
      tourInfoCopier.add(tourLoaderCopier);

      for (Screen screen : screens) {
        if (screen.getType() != ScreenType.Img) {
          AssetFilePath fromScreenEditFilePath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Screen,
            screen.getAssetPrefixHash(),
            S3Config.getEntityFiles().editFile().filename());
          AssetFilePath toScreenEditFilePath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Screen,
            screen.getAssetPrefixHash(),
            S3Config.getEntityFiles().publishedEditFile().filename(nextVersion));

          Callable<AssetFilePath> screenEditCopier = () -> s3Service.copy(fromScreenEditFilePath, toScreenEditFilePath, Map.of(
            HttpHeaders.CONTENT_TYPE, "application/json",
            HttpHeaders.CACHE_CONTROL, S3Config.getCachePolicyStr(S3Config.getEntityFiles().publishedLoaderFile().cachePolicy())
          ));
          tourInfoCopier.add(screenEditCopier);
        }
      }
      Utils.runInParallel(tourInfoCopier.toArray(new Callable[0]));

      tour.setLastPublishedDate(Utils.getCurrentUtcTimestamp());
      tour.setPublishedVersion(nextVersion);

      RespTourWithScreens respTourWithScreens = RespTourWithScreens.from(tour, commonConfig);
      ApiResp<RespTourWithScreens> apiResp = ApiResp.<RespTourWithScreens>builder().data(respTourWithScreens).build();
      String tourResp = objectMapper.writeValueAsString(apiResp);
      uploadDataFileToS3(tourResp, tour.getRid(), S3Config.getEntityFiles().publishedTourEntityFile(), S3Config.AssetType.PublishedTour);

      Tour savedTour = tourRepo.save(tour);
      return RespTour.from(savedTour);
    } catch (Exception e) {
      log.error("Error while trying to publish tour", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while trying to publish tour");
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void uploadTourManifestToS3(Tour tour) {
    TourManifest tourManifest = TourManifest.builder()
      .version(1)
      .name(tour.getDisplayName())
      .url(appConfig.getUrlForDemo() + "/" + tour.getRid())
      .build();
    List<ScreenAssets> screenAssets = new ArrayList<>();
    try {
      S3Config.PathConfigForClient pathConfigForClient = s3Config.getPathConfigForClient();
      String commonAssetPath = pathConfigForClient.commonAsset();
      for (Screen screen : tour.getScreens()) {
        if (StringUtils.isBlank(screen.getThumbnail())) continue;
        ScreenAssets screenAsset = ScreenAssets.builder()
          .name(screen.getDisplayName())
          .url(screen.getUrl())
          .thumbnail(commonAssetPath + screen.getThumbnail())
          .icon(screen.getIcon())
          .build();
        screenAssets.add(screenAsset);
      }
      tourManifest.setScreenAssets(screenAssets);
      String tourScreenInfoAsString = objectMapper.writeValueAsString(tourManifest);
      uploadDataFileToS3(tourScreenInfoAsString, tour.getRid(), S3Config.getEntityFiles().manifestFile(), S3Config.AssetType.PublishedTour);
    } catch (Exception e) {
      throw new RuntimeException("Something went wrong while sending tour screen info to s3 " + e.getMessage());
    }
  }

  private List<Tour> getOnboardingTours() {
    String onboardingTourIds = settings.getOnboardingTourIds();
    if (onboardingTourIds != null && !StringUtils.isBlank(onboardingTourIds)) {
      List<Long> parsedOnboardingTourIds = Arrays.stream(settings.getOnboardingTourIds().trim().split(","))
        .map(Long::valueOf)
        .collect(Collectors.toList());
      return tourRepo.findAllByIdIn(parsedOnboardingTourIds);
    }
    return null;
  }

  @Transactional(readOnly = true)
  public List<OnboardingTourForPrev> getOnboardingToursForPreview(User user) {
    List<Tour> onboardingTours = getOnboardingTours();
    if (onboardingTours == null) return List.of();
    return onboardingTours.stream().map(tour -> new OnboardingTourForPrev(tour.getRid(), tour.getDisplayName(), tour.getDescription())).collect(Collectors.toList());
  }

  @Transactional
  public List<RespTourWithScreens> createOnboardingTourInUserAccount(User user) {
    List<RespTourWithScreens> respOnboardingTours = new ArrayList<>();
    try {
      List<Tour> onboardingTours = getOnboardingTours();
      if (onboardingTours != null) {
        for (Tour onboardingTour : onboardingTours) {
          respOnboardingTours.add(this.duplicateTour(onboardingTour, user,
            newTour -> newTour.onboarding(true).belongsToOrg(user.getBelongsToOrg()), true));
        }
      } else {
        log.error(ONBOARDING_ERR_NO_TOUR_IDS);
        Sentry.captureMessage(ONBOARDING_ERR_NO_TOUR_IDS);
      }
      return respOnboardingTours;
    } catch (Exception e) {
      log.error("Something went wrong while trying to create onboarding tours for an organisation", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while trying to create an onboarding tours for organisation");
    }
  }

  @Transactional
  public RespTour updateTourProperty(ReqTourPropUpdate body, User userEntity) {
    Tour tour = getEntityByRIdWithAuthValidation(Tour.class, body.tourRid(), userEntity);
    body.inProgress().ifPresent(tour::setInProgress);
    return RespTour.from(tour);
  }

  @Transactional
  public String getAssetPathForTour(Long tourId) {
    Optional<Tour> maybeTour = tourRepo.findById(tourId);
    if (maybeTour.isEmpty()) {
      log.warn("Tour with id {} not found", tourId);
      return "";
    }
    AssetFilePath tourAssetFilePath = s3Config.getQualifiedPathFor(
      S3Config.AssetType.Tour,
      maybeTour.get().getAssetPrefixHash(),
      S3Config.getEntityFiles().tourDataFile().filename());
    return tourAssetFilePath.getS3UriToFile();
  }

  public RespTour getTourById(Long id) {
    Optional<Tour> maybeTour = tourRepo.findById(id);
    return maybeTour.map(RespTour::from).orElse(null);
  }
}
