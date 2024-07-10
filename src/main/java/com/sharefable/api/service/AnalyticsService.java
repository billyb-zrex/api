package com.sharefable.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharefable.api.common.AssetFilePath;
import com.sharefable.api.common.TopLevelEntityType;
import com.sharefable.api.common.Utils;
import com.sharefable.api.config.AppSettings;
import com.sharefable.api.config.S3Config;
import com.sharefable.api.entity.*;
import com.sharefable.api.repo.*;
import com.sharefable.api.transport.*;
import com.sharefable.api.transport.req.ReqAddOrUpdateLeadInfo;
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
import java.util.Optional;
import java.util.Set;

@Service
@Slf4j
public class AnalyticsService extends ServiceBase {
  private static final String TABLE_NAME = "analytics_cta_clicked";
  private static final String TABLE_PREFIX = "analytics_";
  private static final String LIFETIME = "20230100";
  private final ObjectMapper mapper = new ObjectMapper();
  private final AnalyticsMetricsRepo analyticsMetricsRepo;
  private final AnalyticsAnnClickRepo analyticsAnnClickRepo;
  private final AnalyticsConversionRepo analyticsConversionRepo;
  private final AnalyticsUserAidMappingRepo analyticsUserAidMappingRepo;
  private final AnalyticsCtaClickedRepo analyticsCtaClickedRepo;
  private final EntityService tourService;
  private final S3Config s3Config;
  private final DemoEntityRepo demoEntityRepo;
  private final HouseLeadInfoRepo houseLeadInfoRepo;
  private final LeadInfoVendorMappingRepo leadInfoVendorMappingRepo;

  @Autowired
  public AnalyticsService(
    AnalyticsMetricsRepo analyticsMetricsRepo,
    AnalyticsAnnClickRepo analyticsAnnClickRepo,
    AnalyticsConversionRepo analyticsConversionRepo,
    EntityService tourService,
    AnalyticsUserAidMappingRepo analyticsUserAidMappingRepo,
    AnalyticsCtaClickedRepo analyticsCtaClickedRepo,
    DemoEntityRepo demoEntityRepo,
    AppSettings settings,
    S3Service s3Service,
    ScreenRepo screenRepo,
    S3Config s3Config, HouseLeadInfoRepo houseLeadInfoRepo,
    LeadInfoVendorMappingRepo leadInfoVendorMappingRepo) {
    super(settings, s3Service, s3Config, screenRepo, demoEntityRepo);
    this.analyticsMetricsRepo = analyticsMetricsRepo;
    this.analyticsAnnClickRepo = analyticsAnnClickRepo;
    this.analyticsConversionRepo = analyticsConversionRepo;
    this.analyticsUserAidMappingRepo = analyticsUserAidMappingRepo;
    this.analyticsCtaClickedRepo = analyticsCtaClickedRepo;
    this.tourService = tourService;
    this.demoEntityRepo = demoEntityRepo;
    this.s3Config = s3Config;
    this.houseLeadInfoRepo = houseLeadInfoRepo;
    this.leadInfoVendorMappingRepo = leadInfoVendorMappingRepo;
  }

  @Transactional(readOnly = true)
  public RespTourView getTotalVisitorsForTour(String rid, Integer days, User user) {
    DemoEntity demoEntity = tourService.getEntityByRIdWithAuthValidation(DemoEntity.class, rid, user, TopLevelEntityType.TOUR);
    List<AnalyticsMetrics> metrics = analyticsMetricsRepo.findByTourId(demoEntity.getId());
    if (metrics.isEmpty()) {
      log.warn("No analytics entry is present for this tour id {} at the time", demoEntity.getId());
      return RespTourView.Empty();
    }
    String dateBeforeCertainDays = days > 0 ? Utils.calculateParticularUtcDateFromCurrentUtc(days) : LIFETIME;
    Long totalVisitors = analyticsMetricsRepo.findTotalVisitorsForTourId(demoEntity.getId(), dateBeforeCertainDays);
    Long uniqueViews = analyticsMetricsRepo.findUniqueVisitorsForTourId(demoEntity.getId(), dateBeforeCertainDays);
    List<TotalVisitorsByYmd> totalVisitorsByYmd = analyticsMetricsRepo.findTotalVisitorsByYmd(demoEntity.getId(), dateBeforeCertainDays);
    return RespTourView.builder()
      .tourId(demoEntity.getId())
      .totalViews(totalVisitors)
      .uniqueViews(uniqueViews)
      .totalVisitorsByYmd(totalVisitorsByYmd)
      .build();
  }

  @Transactional(readOnly = true)
  public RespTourAnnViews getAnnViews(String rid, Integer days, User user) {
    DemoEntity demoEntity = tourService.getEntityByRIdWithAuthValidation(DemoEntity.class, rid, user, TopLevelEntityType.TOUR);
    List<AnalyticsAnnClick> annClick = analyticsAnnClickRepo.findByTourId(demoEntity.getId());
    if (annClick.isEmpty()) {
      log.warn("No analytics entry is present for this tour id {} at the time", demoEntity.getId());
      return RespTourAnnViews.Empty();
    }
    String dateBeforeCertainDays = days > 0 ? Utils.calculateParticularUtcDateFromCurrentUtc(days) : LIFETIME;
    List<TourAnnWithViews> tourAnnWithViews = analyticsAnnClickRepo.findTotalViewsForAnn(demoEntity.getId(), dateBeforeCertainDays);
    if (tourAnnWithViews.isEmpty()) {
      return RespTourAnnViews.Empty();
    }
    return RespTourAnnViews.builder()
      .tourId(demoEntity.getId())
      .tourAnnWithViews(tourAnnWithViews)
      .build();

  }

  @Transactional(readOnly = true)
  public RespTourAnnWithPercentile getTimeSpentForEachAnnotation(String rid, Integer days, User user) {
    DemoEntity demoEntity = tourService.getEntityByRIdWithAuthValidation(DemoEntity.class, rid, user, TopLevelEntityType.TOUR);
    List<AnalyticsAnnClick> annClick = analyticsAnnClickRepo.findByTourId(demoEntity.getId());
    if (annClick.isEmpty()) {
      log.warn("No analytics entry is present for this tour id {} at the time", demoEntity.getId());
      return RespTourAnnWithPercentile.Empty();
    }
    String dateBeforeCertainDays = days > 0 ? Utils.calculateParticularUtcDateFromCurrentUtc(days) : LIFETIME;
    List<TourAnnViewsWithPercentile> annInfo = analyticsAnnClickRepo.findTotalViewWithPercentile(demoEntity.getId(), dateBeforeCertainDays);
    if (annInfo.isEmpty()) {
      return RespTourAnnWithPercentile.Empty();
    }
    return RespTourAnnWithPercentile.builder()
      .tourAnnInfo(annInfo)
      .build();
  }

  @Transactional(readOnly = true)
  public RespConversion getTourConversion(String rid, Integer days, User user) {
    DemoEntity demoEntity = tourService.getEntityByRIdWithAuthValidation(DemoEntity.class, rid, user, TopLevelEntityType.TOUR);
    List<AnalyticsConversion> tourConversion = analyticsConversionRepo.findByTourId(demoEntity.getId());
    if (tourConversion.isEmpty()) {
      log.warn("No analytics entry is present for this tour id {} at the time", demoEntity.getId());
      return RespConversion.Empty();
    }
    String dateBeforeCertainDays = days > 0 ? Utils.calculateParticularUtcDateFromCurrentUtc(days) : LIFETIME;
    List<ButtonClicks> buttonInfo = analyticsConversionRepo.findClicksForAllButtonIdInATour(demoEntity.getId(), dateBeforeCertainDays);
    if (buttonInfo.isEmpty()) {
      return RespConversion.Empty();
    }
    return RespConversion.builder()
      .tourId(demoEntity.getId())
      .buttonsWithTotalClicks(buttonInfo)
      .build();
  }

  @Transactional(readOnly = true)
  public RespTourLeads getLeadsForATour(String rid, Integer days, User user) {
    DemoEntity demoEntity = tourService.getEntityByRIdWithAuthValidation(DemoEntity.class, rid, user, TopLevelEntityType.TOUR);
    List<AnalyticsUserAidMapping> analyticsUserAidMapping = analyticsUserAidMappingRepo.findByTourId(demoEntity.getId());
    if (analyticsUserAidMapping.isEmpty()) {
      return RespTourLeads.Empty();
    }
    String dateBeforeCertainDays = days > 0 ? Utils.calculateParticularUtcDateFromCurrentUtc(days) : LIFETIME;
    List<TourLeads> allTourLeads = analyticsUserAidMappingRepo.findLeadsByYmd(demoEntity.getId(), dateBeforeCertainDays);
    Long uniqueEmails = analyticsUserAidMappingRepo.findUniqueEmails(demoEntity.getId(), dateBeforeCertainDays);
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
    DemoEntity demoEntity = tourService.getEntityByRIdWithAuthValidation(DemoEntity.class, rid, user, TopLevelEntityType.TOUR);
    try {
      Pair<String, String> leadAnalyticsPath = getLeadActivityPath(demoEntity.getId(), aid);
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

  public void logUserEvents(String sub, String userEventLogs) {
    try {
      String tableName = TABLE_PREFIX + sub;
      if (tableName.equals(TABLE_NAME)) {
        AnalyticsCtaClicked analyticsCtaClicked = mapper.readValue(userEventLogs, AnalyticsCtaClicked.class);
        analyticsCtaClickedRepo.save(analyticsCtaClicked);
      }
    } catch (Exception e) {
      log.error("Something wrong while sending {} events to database", sub, e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something wrong while sending events to database");
    }
  }

  @Transactional
  public void updateLeadInfo(ReqAddOrUpdateLeadInfo body) {
    Long tourId = body.tourId();
    Optional<DemoEntity> maybeTour = demoEntityRepo.findById(tourId);
    if (maybeTour.isEmpty()) {
      log.warn("Tour with id {} not found", tourId);
      return;
    }

    DemoEntity demoEntity = maybeTour.get();
    Long orgId = demoEntity.getBelongsToOrg();


    Optional<HouseLeadInfo> maybeHouseLeadInfo = houseLeadInfoRepo.findByOrgIdAndLeadEmailId(orgId, body.emailId());
    LeadInfoVendorMapping leadInfoVendorMapping;
    if (maybeHouseLeadInfo.isEmpty()) {
      HouseLeadInfo houseLeadInfo = HouseLeadInfo.builder()
        .orgId(orgId)
        .leadEmailId(body.emailId())
        .info360(Set.of(
          Lead360.Empty(demoEntity.getId()),
          Lead360.Empty()
        ))
        .build();
      leadInfoVendorMapping =
        LeadInfoVendorMapping.builder()
          .houseLeadInfo(houseLeadInfo)
          .infoKey(body.key())
          .infoValue(body.value())
          .build();
    } else {
      Optional<LeadInfoVendorMapping> maybeLeadInfoVendorMapping = leadInfoVendorMappingRepo.findLeadInfoVendorMappingByHouseLeadInfoAndInfoKey(
        maybeHouseLeadInfo.get(),
        body.key()
      );
      if (maybeLeadInfoVendorMapping.isEmpty()) {
        leadInfoVendorMapping = LeadInfoVendorMapping.builder()
          .houseLeadInfo(maybeHouseLeadInfo.get())
          .infoKey(body.key())
          .infoValue(body.value())
          .build();
        leadInfoVendorMapping.setInfoValue(body.value());
      } else {
        leadInfoVendorMapping = maybeLeadInfoVendorMapping.get();
        leadInfoVendorMapping.setInfoValue(body.value());
      }
    }
    leadInfoVendorMappingRepo.save(leadInfoVendorMapping);
  }
}
