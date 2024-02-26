package com.sharefable.api.service;

import com.sharefable.api.common.SumViews;
import com.sharefable.api.common.Utils;
import com.sharefable.api.entity.*;
import com.sharefable.api.repo.AnalyticsAnnClickRepo;
import com.sharefable.api.repo.AnalyticsConversionRepo;
import com.sharefable.api.repo.AnalyticsMetricsRepo;
import com.sharefable.api.transport.ButtonClicks;
import com.sharefable.api.transport.TotalVisitorsByYmd;
import com.sharefable.api.transport.TourAnnViewsWithPercentile;
import com.sharefable.api.transport.TourAnnWithViews;
import com.sharefable.api.transport.resp.RespConversion;
import com.sharefable.api.transport.resp.RespTourAnnViews;
import com.sharefable.api.transport.resp.RespTourAnnWithPercentile;
import com.sharefable.api.transport.resp.RespTourView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class AnalyticsService {
    private final AnalyticsMetricsRepo analyticsMetricsRepo;
    private final AnalyticsAnnClickRepo analyticsAnnClickRepo;
    private final AnalyticsConversionRepo analyticsConversionRepo;
    private final TourService tourService;

    @Autowired
    public AnalyticsService(AnalyticsMetricsRepo analyticsMetricsRepo, AnalyticsAnnClickRepo analyticsAnnClickRepo, AnalyticsConversionRepo analyticsConversionRepo, TourService tourService) {
        this.analyticsMetricsRepo = analyticsMetricsRepo;
        this.analyticsAnnClickRepo = analyticsAnnClickRepo;
        this.analyticsConversionRepo = analyticsConversionRepo;
        this.tourService = tourService;
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
}
