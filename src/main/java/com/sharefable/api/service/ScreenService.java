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
import com.sharefable.api.transport.ReqCopyScreen;
import com.sharefable.api.transport.ReqNewScreen;
import com.sharefable.api.transport.RespScreen;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ScreenService extends ServiceBase {
    private final ScreenRepo screenRepo;
    private final S3Config s3Config;
    private final S3Service s3Service;
    private final TourRepo tourRepo;

    @Autowired
    public ScreenService(ScreenRepo screenRepo, S3Service s3Service, S3Config s3Config, TourRepo tourRepo, AppSettings settings) {
        super(settings, s3Service, s3Config);
        this.s3Service = s3Service;
        this.s3Config = s3Config;
        this.screenRepo = screenRepo;
        this.tourRepo = tourRepo;
    }

    @Transactional
    public RespScreen createNewScreen(ReqNewScreen req, User createdByUser) {
        String prefixHash = Utils.createUuidWord();
        Callable<Optional<AssetFilePath>> dataFileUploader =
            () -> Optional.ofNullable(uploadDataFileToS3(req.body(), prefixHash, s3Config.getFileNames().dataFile(), S3Config.AssetType.Screen));

        Callable<Optional<AssetFilePath>> thumbnailUploader =
            () -> uploadBase64ImageToS3(req.thumbnail(), S3Config.AssetType.Common);

        try {
            List<Optional<AssetFilePath>> assetFiles = Utils.runInParallel(dataFileUploader, thumbnailUploader);
            Optional<AssetFilePath> thumbnailFile = assetFiles.get(1);

            String thumbnailFilePath = null;
            if (thumbnailFile.isPresent()) {
                thumbnailFilePath = thumbnailFile.get().getFilePath();
            }

            Screen screen = Screen.builder()
                .createdBy(createdByUser)
                .displayName(req.name())
                .rid(Utils.createReadableId(req.name()))
                .parentScreenId(req.normalizedParentId())
                .assetPrefixHash(prefixHash)
                .belongsToOrg(createdByUser.getBelongsToOrg())
                .icon(req.favIcon().orElse(null))
                .url(req.url())
                .thumbnail(thumbnailFilePath)
                .build();

            Screen storedScreen = screenRepo.save(screen);
            return RespScreen.from(storedScreen);
        } catch (Exception e) {
            log.error("Error while uploading file to s3. Message: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Something went wrong when saving screen");
        }
    }

    public RespScreen copyFromParentScreen(ReqCopyScreen body, User userEntity) {
        Long parentId = body.parentId();
        String tourRid = body.tourRid();

        Optional<Screen> maybeScreen = screenRepo.findById(parentId);
        Optional<Tour> maybeTour = tourRepo.findByRid(tourRid);

        if (maybeScreen.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Screen with id %s not found", parentId));
        }
        if (maybeTour.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Tour with id %s not found", tourRid));
        }

        Tour tour = maybeTour.get();
        Set<Tour> tours = new HashSet<>();
        tours.add(tour);

        Screen parentScreen = maybeScreen.get();
        String prefixHash = Utils.createUuidWord();

        AssetFilePath fromDataFilePath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Screen,
            parentScreen.getAssetPrefixHash(),
            s3Config.getFileNames().dataFile());
        AssetFilePath fromThumbnailPath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Common, parentScreen.getThumbnail());

        AssetFilePath toDataFilePath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Screen, prefixHash, s3Config.getFileNames().dataFile());
        AssetFilePath toThumbnailPath = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Common, UUID.randomUUID().toString());

        Callable<AssetFilePath> dataFileCopier = () -> s3Service.copy(fromDataFilePath, toDataFilePath);
        Callable<AssetFilePath> thumbnailCopier = () -> s3Service.copy(fromThumbnailPath, toThumbnailPath);
        Callable<AssetFilePath> editFileUploader = () -> uploadTemplateFileToS3(prefixHash, DATA_FILE_TYPE.SCREEN_EDIT);

        try {
            List<AssetFilePath> assetFiles = Utils.runInParallel(dataFileCopier, thumbnailCopier, editFileUploader);
            AssetFilePath thumbnailFile = assetFiles.get(1);

            Screen screen = Screen.builder()
                .rid(Utils.createReadableId(parentScreen.getDisplayName()))
                .createdBy(userEntity)
                .url(parentScreen.getUrl())
                .displayName(parentScreen.getDisplayName())
                .assetPrefixHash(prefixHash)
                .belongsToOrg(userEntity.getBelongsToOrg())
                .icon(parentScreen.getIcon())
                .thumbnail(thumbnailFile.getFilePath())
                .tours(tours)
                .parentScreenId(parentId)
                .build();

            Screen storedScreen = screenRepo.save(screen);
            return RespScreen.from(storedScreen);
        } catch (Exception e) {
            log.error("Error while copying file from parent screen to child screen. Message: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Something went wrong when saving screen");
        }
    }

    public List<RespScreen> getAllScreensForOrg(Long orgId) {
        List<Screen> screens = screenRepo.findAllByBelongsToOrgOrderByUpdatedAtDesc(orgId);
        return screens.stream().map(RespScreen::from).collect(Collectors.toList());
    }

    public Optional<RespScreen> getScreenByRid(String rid) {
        Optional<Screen> maybeScreen = screenRepo.findByRid(rid);
        return maybeScreen.map(RespScreen::from);
    }

}
