package com.sharefable.api.service;

import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.Screen;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.TourRepo;
import com.sharefable.api.transport.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TourService extends ServiceBase {
    private final TourRepo tourRepo;

    @Autowired
    public TourService(TourRepo tourRepo, AppSettings settings, S3Service s3Service, S3Config s3Config) {
        super(settings, s3Service, s3Config);
        this.tourRepo = tourRepo;
    }

    @Transactional
    public List<RespTour> getAllToursForOrg(Long orgId) {
        List<Tour> tours = tourRepo.findAllByBelongsToOrgOrderByUpdatedAtDesc(orgId);
        return tours.stream().map(RespTour::from).collect(Collectors.toList());
    }

    public RespTour createNewTour(ReqNewTour req, User createdByUser) {
        String prefixHash = Utils.createUuidWord();
        uploadTemplateFileToS3(prefixHash, DATA_FILE_TYPE.TOUR_INDEX);

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
    public RespTour updateEditForTour(ReqRecordEdit body, User userEntity) {
        Tour tour = getTourByRIdWithAuthValidation(body.rid(), userEntity);

        uploadDataFileToS3(
            body.editData(),
            tour.getAssetPrefixHash(),
            S3Config.getEntityFiles().dataFile(),
            S3Config.AssetType.Tour);


        tour.setUpdatedAt(Utils.getCurrentUtcTimestamp());
        Tour updatedTour = tourRepo.save(tour);
        return RespTour.from(updatedTour);
    }

    private Tour getTourByRIdWithAuthValidation(String rid, User user) {
        Optional<Tour> maybeTour = tourRepo.findByRid(rid);
        if (maybeTour.isEmpty()) {
            log.error("Can't update edit for tour {} as it's not found", rid);
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "");
        }
        Tour tour = maybeTour.get();
        if (!Objects.equals(tour.getBelongsToOrg(), user.getBelongsToOrg())) {
            log.error("Can't update edit for tour {} as it's belong to different org. Requested by user {}, belongs to org {}",
                rid, user.getId(), tour.getBelongsToOrg());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not enough permission");
        }
        return tour;
    }

    public RespTour renameTour(ReqRenameGeneric body, User userEntity) {
        Tour tour = getTourByRIdWithAuthValidation(body.rid(), userEntity);
        String newName = body.newName();
        tour.setDisplayName(newName);
        tour.setRid(Utils.createReadableId(newName));
        Tour updatedTour = tourRepo.save(tour);
        return RespTour.from(updatedTour);
    }
}
