package com.sharefable.api.service;

import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.Tour;
import com.sharefable.api.entity.User;
import com.sharefable.api.repo.TourRepo;
import com.sharefable.api.transport.ReqNewTour;
import com.sharefable.api.transport.RespTour;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TourService extends ServiceBase {
    private static final String PATH_TO_SCHEMA_FILE = "/data-schema/v=%s/tour/index.json";
    private final TourRepo tourRepo;
    private final AppSettings settings;

    private final S3Config s3Config;

    @Autowired
    public TourService(TourRepo tourRepo, AppSettings settings, S3Service s3Service, S3Config s3Config) {
        super(s3Service, s3Config);
        this.tourRepo = tourRepo;
        this.settings = settings;
        this.s3Config = s3Config;
    }

    @Transactional
    public List<RespTour> getAllToursForOrg(Long orgId) {
        List<Tour> tours = tourRepo.findAllByBelongsToOrgOrderByUpdatedAtDesc(orgId);
        return tours.stream().map(RespTour::from).collect(Collectors.toList());
    }

    public RespTour createNewTour(ReqNewTour req, User createdByUser) {
        String prefixHash = Utils.createUuidWord();

        String schemaVersion = settings.currentSchemaVersion().toValue();
        String resourcePath = String.format(PATH_TO_SCHEMA_FILE, schemaVersion);
        try (InputStream resourceAsStream = getClass().getResourceAsStream(resourcePath)) {
            if (resourceAsStream == null) {
                log.error("No default data file is present while creating tour. Can't find schema file with path = {}", resourcePath);
                throw new RuntimeException("Can't find schema file");
            }
            String fileContent = IOUtils.toString(resourceAsStream, StandardCharsets.UTF_8);
            uploadDataFileToS3(fileContent, prefixHash, s3Config.getFileNames().dataFile(), S3Config.AssetType.Tour);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        Tour tour = Tour.builder()
            .createdBy(createdByUser)
            .displayName(req.name())
            .description(req.description().orElse(""))
            .rid(Utils.createReadableId(req.name()))
            .assetPrefixHash(prefixHash)
            .belongsToOrg(createdByUser.getBelongsToOrg().getId())
            .build();

        Tour storedTour = tourRepo.save(tour);
        return RespTour.from(storedTour);
    }

    public Optional<RespTour> getTourByRid(String rid) {
        Optional<Tour> maybeTour = tourRepo.findByRid(rid);
        return maybeTour.map(RespTour::from);
    }
}
