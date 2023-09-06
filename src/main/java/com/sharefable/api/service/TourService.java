package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.Screen;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.ScreenRepo;
import com.sharefable.api.repo.TourRepo;
import com.sharefable.api.transport.EditTour;
import com.sharefable.api.transport.req.*;
import com.sharefable.api.transport.resp.RespTour;
import com.sharefable.api.transport.resp.RespTourWithScreens;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TourService extends ServiceBase {
    private final TourRepo tourRepo;
    private final S3Config s3Config;
    private final ScreenService screenService;

    @Autowired
    public TourService(TourRepo tourRepo, AppSettings settings, S3Service s3Service, S3Config s3Config, ScreenRepo screenRepo, ScreenService screenService) {
        super(settings, s3Service, s3Config, screenRepo, tourRepo);
        this.tourRepo = tourRepo;
        this.s3Config = s3Config;
        this.screenService = screenService;
    }

    @Transactional
    public List<RespTour> getAllToursForOrg(Long orgId) {
        List<Tour> tours = tourRepo.findAllByBelongsToOrgOrderByUpdatedAtDesc(orgId);
        return tours.stream().map(RespTour::from).collect(Collectors.toList());
    }

    public RespTour createNewTour(ReqNewTour req, User createdByUser) {
        String prefixHash = Utils.createUuidWord();
        uploadTemplateFileToS3(prefixHash, DATA_FILE_TYPE.TOUR_INDEX);
        uploadTemplateFileToS3(prefixHash, DATA_FILE_TYPE.TOUR_LOADER);

        Tour tour = Tour.builder()
            .createdBy(createdByUser)
            .displayName(req.name())
            .description(req.description().orElse(""))
            .rid(Utils.createReadableId(req.name()))
            .assetPrefixHash(prefixHash)
            .belongsToOrg(createdByUser.getBelongsToOrg())
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
            fileTobeEdited == EditTour.INDEX ? S3Config.getEntityFiles().dataFile() : S3Config.getEntityFiles().loaderFile(),
            S3Config.AssetType.Tour);

        tour.setUpdatedAt(Utils.getCurrentUtcTimestamp());
        Tour updatedTour = tourRepo.save(tour);
        return RespTour.from(updatedTour);
    }

    public RespTour renameTour(ReqRenameGeneric body, User userEntity) {
        Tour tour = getEntityByRIdWithAuthValidation(Tour.class, body.rid(), userEntity);
        String newName = body.newName();
        tour.setDisplayName(newName);
        tour.setRid(Utils.createReadableId(newName));
        Tour updatedTour = tourRepo.save(tour);
        return RespTour.from(updatedTour);
    }

    @Transactional
    public RespTourWithScreens duplicateTour(ReqDuplicateTour body, User user) {
        Tour fromTour = getEntityByRIdWithAuthValidation(Tour.class, body.fromTourRid(), user);
        AssetFilePath fromTourDataFilePath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Tour,
            fromTour.getAssetPrefixHash(),
            S3Config.getEntityFiles().dataFile().filename());
        AssetFilePath fromTourLoaderFilePath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Tour,
            fromTour.getAssetPrefixHash(),
            S3Config.getEntityFiles().loaderFile().filename());

        String prefixHash = Utils.createUuidWord();
        copyDataFileToS3(fromTourDataFilePath, prefixHash, DATA_FILE_TYPE.TOUR_INDEX);
        copyDataFileToS3(fromTourLoaderFilePath, prefixHash, DATA_FILE_TYPE.TOUR_LOADER);

        String rid = Utils.createReadableId(body.duplicateTourName());
        Tour tour = Tour.builder()
            .assetPrefixHash(prefixHash)
            .belongsToOrg(fromTour.getBelongsToOrg())
            .rid(rid)
            .displayName(body.duplicateTourName())
            .description(fromTour.getDescription())
            .createdBy(user)
            .build();
        Tour savedTour = tourRepo.save(tour);

        Set<Screen> sourceScreens = fromTour.getScreens();
        Set<Screen> clonedScreens = new HashSet<>(sourceScreens.size());
        Map<String, String> sourceAndClonedScreenIdMap = new HashMap<>(sourceScreens.size());
        for (Screen sourceScreen : sourceScreens) {
            Screen clonedScreen = screenService.cloneScreen(sourceScreen, user, savedTour);
            clonedScreens.add(clonedScreen);
            sourceAndClonedScreenIdMap.put(Long.toString(sourceScreen.getId()), Long.toString(clonedScreen.getId()));
        }
        savedTour.setScreens(clonedScreens);
        RespTourWithScreens resp = RespTourWithScreens.from(savedTour);
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
}
