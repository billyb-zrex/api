package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.Screen;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.ScreenRepo;
import com.sharefable.api.transport.NewScreenReq;
import com.sharefable.api.transport.NewScreenResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

@Service
@Slf4j
public class ScreenService extends ServiceBase {
    private final ScreenRepo screenRepo;

    @Autowired
    public ScreenService(ScreenRepo screenRepo, S3Service s3Service, S3Config s3Config) {
        super(s3Service, s3Config);
        this.screenRepo = screenRepo;
    }

    @Transactional
    public NewScreenResp createNewScreen(NewScreenReq req, User createdByUser) {
        String prefixHash = Utils.createUuidWord();
        Callable<Optional<AssetFilePath>> dataFileUploader =
            () -> Optional.ofNullable(uploadDataFileToS3(req.body(), prefixHash, "index.json", S3Config.AssetType.Screen));

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
                .rId(Utils.createReadableId(req.name()))
                .parentScreenId(req.normalizedParentId())
                .assetPrefixHash(prefixHash)
                .belongsToOrg(createdByUser.getBelongsToOrg().getId())
                .icon(req.favIcon().orElse(null))
                .url(req.url())
                .thumbnail(thumbnailFilePath)
                .build();

            Screen storedScreen = screenRepo.save(screen);
            return NewScreenResp.from(storedScreen);
        } catch (Exception e) {
            log.error("Error while uploading file to s3. Message: {}", e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Something went wrong when saving screen");
        }
    }
}
