package com.sharefable.api.service;

import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.SumViews;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.*;
import com.sharefable.api.repo.*;
import com.sharefable.api.transport.*;
import com.sharefable.api.transport.req.ReqLeadActivityDataPost;
import com.sharefable.api.transport.resp.*;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
public class AnalyticsService extends ServiceBase {
    private final AnalyticsMetricsRepo analyticsMetricsRepo;
    private final AnalyticsAnnClickRepo analyticsAnnClickRepo;
    private final AnalyticsConversionRepo analyticsConversionRepo;
    private final AnalyticsUserAidMappingRepo analyticsUserAidMappingRepo;
    private final TourService tourService;
    private final S3Config s3Config;

    @Autowired
    public AnalyticsService(
        AnalyticsMetricsRepo analyticsMetricsRepo,
        AnalyticsAnnClickRepo analyticsAnnClickRepo,
        AnalyticsConversionRepo analyticsConversionRepo,
        TourService tourService,
        AnalyticsUserAidMappingRepo analyticsUserAidMappingRepo,
        TourRepo tourRepo,
        AppSettings settings,
        S3Service s3Service,
        ScreenRepo screenRepo,
        S3Config s3Config) {
        super(settings, s3Service, s3Config, screenRepo, tourRepo);
        this.analyticsMetricsRepo = analyticsMetricsRepo;
        this.analyticsAnnClickRepo = analyticsAnnClickRepo;
        this.analyticsConversionRepo = analyticsConversionRepo;
        this.analyticsUserAidMappingRepo = analyticsUserAidMappingRepo;
        this.tourService = tourService;
        this.s3Config = s3Config;
    }

    @Transactional(readOnly = true)
    public RespTourView getTotalVisitorsForTour(String rid, Integer days, User user) {
        Tour tour = tourService.getEntityByRIdWithAuthValidation(Tour.class, rid, user);
        List<AnalyticsMetrics> metrics = analyticsMetricsRepo.findByTourId(tour.getId());
        if (metrics.isEmpty()) {
            log.warn("No analytics entry is present for this tour id {} at the time", tour.getId());
            return RespTourView.Empty();
        }
        String dateBeforeCertainDays = Utils.calculateParticularUtcDateFromCurrentUtc(days);
        SumViews sumViews = analyticsMetricsRepo.findSumOfAllVisitorsForTourId(tour.getId(), dateBeforeCertainDays);
        List<TotalVisitorsByYmd> totalVisitorsByYmd = analyticsMetricsRepo.findTotalVisitorsByYmd(tour.getId(), dateBeforeCertainDays);
        return RespTourView.builder()
            .tourId(tour.getId())
            .totalViews(sumViews)
            .totalVisitorsByYmd(totalVisitorsByYmd)
            .build();
    }

    @Transactional(readOnly = true)
    public RespTourAnnViews getAnnViews(String rid, Integer days, User user) {
        Tour tour = tourService.getEntityByRIdWithAuthValidation(Tour.class, rid, user);
        List<AnalyticsAnnClick> annClick = analyticsAnnClickRepo.findByTourId(tour.getId());
        if (annClick.isEmpty()) {
            log.warn("No analytics entry is present for this tour id {} at the time", tour.getId());
            return RespTourAnnViews.Empty();
        }
        String dateBeforeCertainDays = Utils.calculateParticularUtcDateFromCurrentUtc(days);
        List<TourAnnWithViews> tourAnnWithViews = analyticsAnnClickRepo.findTotalViewsForAnn(tour.getId(), dateBeforeCertainDays);
        if (tourAnnWithViews.isEmpty()) {
            return RespTourAnnViews.Empty();
        }
        return RespTourAnnViews.builder()
            .tourId(tour.getId())
            .tourAnnWithViews(tourAnnWithViews)
            .build();

    }

    @Transactional(readOnly = true)
    public RespTourAnnWithPercentile getTimeSpentForEachAnnotation(String rid, Integer days, User user) {
        Tour tour = tourService.getEntityByRIdWithAuthValidation(Tour.class, rid, user);
        List<AnalyticsAnnClick> annClick = analyticsAnnClickRepo.findByTourId(tour.getId());
        if (annClick.isEmpty()) {
            log.warn("No analytics entry is present for this tour id {} at the time", tour.getId());
            return RespTourAnnWithPercentile.Empty();
        }
        String dateBeforeCertainDays = Utils.calculateParticularUtcDateFromCurrentUtc(days);
        List<TourAnnViewsWithPercentile> annInfo = analyticsAnnClickRepo.findTotalViewWithPercentile(tour.getId(), dateBeforeCertainDays);
        if (annInfo.isEmpty()) {
            return RespTourAnnWithPercentile.Empty();
        }
        return RespTourAnnWithPercentile.builder()
            .tourAnnInfo(annInfo)
            .build();
    }

    @Transactional(readOnly = true)
    public RespConversion getTourConversion(String rid, Integer days, User user) {
        Tour tour = tourService.getEntityByRIdWithAuthValidation(Tour.class, rid, user);
        List<AnalyticsConversion> tourConversion = analyticsConversionRepo.findByTourId(tour.getId());
        if (tourConversion.isEmpty()) {
            log.warn("No analytics entry is present for this tour id {} at the time", tour.getId());
            return RespConversion.Empty();
        }
        String dateBeforeCertainDays = Utils.calculateParticularUtcDateFromCurrentUtc(days);
        List<ButtonClicks> buttonInfo = analyticsConversionRepo.findClicksForAllButtonIdInATour(tour.getId(), dateBeforeCertainDays);
        if (buttonInfo.isEmpty()) {
            return RespConversion.Empty();
        }
        return RespConversion.builder()
            .tourId(tour.getId())
            .buttonsWithTotalClicks(buttonInfo)
            .build();
    }

    @Transactional(readOnly = true)
    public RespTourLeads getLeadsForATour(String rid, Integer days, User user) {
        Tour tour = tourService.getEntityByRIdWithAuthValidation(Tour.class, rid, user);
        List<AnalyticsUserAidMapping> analyticsUserAidMapping = analyticsUserAidMappingRepo.findByTourId(tour.getId());
        if (analyticsUserAidMapping.isEmpty()) {
            return RespTourLeads.Empty();
        }
        String dateBeforeCertainDays = Utils.calculateParticularUtcDateFromCurrentUtc(days);
        List<TourLeads> allTourLeads = analyticsUserAidMappingRepo.findLeadsByYmd(tour.getId(), dateBeforeCertainDays);
        Long uniqueEmails = analyticsUserAidMappingRepo.findUniqueEmails(tour.getId(), dateBeforeCertainDays);
        if (allTourLeads.isEmpty()) {
            return RespTourLeads.Empty();
        }
        return RespTourLeads.builder()
            .UniqueEmailCount(uniqueEmails)
            .tourLeads(allTourLeads)
            .build();
    }

    public RespLeadActivityUrl uploadLeadActivityToS3(ReqLeadActivityDataPost body) {
        try {
            Pair<String, String> leadAnalyticsPath = getLeadActivityPath(body.tourId(), body.aid());
            String prefix = leadAnalyticsPath.getValue0();
            String s3UriToFile = leadAnalyticsPath.getValue1();
            uploadDataFileToS3(
                body.data(),
                prefix,
                S3Config.getEntityFiles().leadActivityDataFile(),
                S3Config.AssetType.Analytics);

            return RespLeadActivityUrl.builder()
                .leadActivityUrl(s3UriToFile)
                .build();
        } catch (Exception e) {
            log.error("Error while trying to upload activity of a lead to s3.", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong while trying to upload activity of a lead to s3");
        }
    }

    public RespLeadActivityUrl getLeadActivityDataFile(String rid, String aid, User user) {
        Tour tour = tourService.getEntityByRIdWithAuthValidation(Tour.class, rid, user);
        try {
            Pair<String, String> leadAnalyticsPath = getLeadActivityPath(tour.getId(), aid);
            String s3UriToFile = leadAnalyticsPath.getValue1();
            return RespLeadActivityUrl.builder()
                .leadActivityUrl(s3UriToFile)
                .build();
        } catch (Exception e) {
            log.error("Error while trying to get the lead activity s3 uri for tour rid {}.", rid, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
        }
    }

    protected Pair<String, String> getLeadActivityPath(Long tourId, String aid) {
        String prefixHash = tourId + "/" + aid;
        AssetFilePath qualifiedPathFor = s3Config.getQualifiedPathFor(
            S3Config.AssetType.Analytics, prefixHash, S3Config.getEntityFiles().leadActivityDataFile().filename());
        return Pair.with(prefixHash, qualifiedPathFor.getS3UriToFile());
    }

}
