package com.sharefable.analytics.controller.v1;

import com.sharefable.Routes;
import com.sharefable.analytics.entity.MEntityMetrics;
import com.sharefable.analytics.entity.MEntityMetricsDaily;
import com.sharefable.analytics.entity.MEntitySubEntityDistribution;
import com.sharefable.analytics.entity.MHouseLead;
import com.sharefable.analytics.repo.MEntityMetricsDailyRepo;
import com.sharefable.analytics.repo.MEntityMetricsRepo;
import com.sharefable.analytics.repo.MEntitySubEntityDistRepo;
import com.sharefable.analytics.repo.MHouseLeadRepo;
import com.sharefable.analytics.transport.RespEntityMetrics;
import com.sharefable.analytics.transport.RespHouseLead;
import com.sharefable.api.auth.AuthUser;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.v1.DemoEntityController;
import com.sharefable.api.entity.DemoEntity;
import com.sharefable.api.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(Routes.API_V1)
@RequiredArgsConstructor
@Slf4j
public class InsightController2 {
  private final DemoEntityController demoEntityController;
  private final MEntityMetricsRepo mEntityMetricsRepo;
  private final MHouseLeadRepo mHouseLeadRepo;
  private final MEntityMetricsDailyRepo mEntityMetricsDailyRepo;
  private final MEntitySubEntityDistRepo mEntitySubEntityDistRepo;

  @RequestMapping(value = Routes.ENTITY_METRICS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespEntityMetrics> saveActivity(@RequestParam("rid") String rid, @AuthUser User user) {
    DemoEntity entity = demoEntityController.getEntityAfterValidation(rid, user);
    Optional<MEntityMetrics> resp = mEntityMetricsRepo.findById(entity.getId());

    RespEntityMetrics respEntityMetrics = resp
      .map(RespEntityMetrics::from).orElseGet(RespEntityMetrics::empty);

    return ApiResp.<RespEntityMetrics>builder().status(ApiResp.ResponseStatus.Success)
      .data(respEntityMetrics).build();
  }

  @RequestMapping(value = Routes.LEADS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<RespHouseLead>> getLeadsForTour(@RequestParam("rid") String rid, @AuthUser User user) {
    DemoEntity entity = demoEntityController.getEntityAfterValidation(rid, user);
    List<MHouseLead> leads = mHouseLeadRepo.getMHouseLeadsByEntityId(entity.getId());
    List<RespHouseLead> resp = leads.stream().map(RespHouseLead::from).toList();
    return ApiResp.<List<RespHouseLead>>builder().status(ApiResp.ResponseStatus.Success)
      .data(resp).build();
  }

  @RequestMapping(value = Routes.ENTITY_METRICS_DAILY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<MEntityMetricsDaily>> getEntityMetricsDaily(@RequestParam("rid") String rid, @AuthUser User user) {
    DemoEntity entity = demoEntityController.getEntityAfterValidation(rid, user);
    List<MEntityMetricsDaily> resp = mEntityMetricsDailyRepo.getMEntityMetricsDailiesByEntityId(entity.getId());
    return ApiResp.<List<MEntityMetricsDaily>>builder().status(ApiResp.ResponseStatus.Success)
      .data(resp).build();
  }


  @RequestMapping(value = Routes.ENTITY_SUBENTITY_DIST_METRICS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<List<MEntitySubEntityDistribution>> getEntitySubEntityMetricDistribution(@RequestParam("rid") String rid, @AuthUser User user) {
    DemoEntity entity = demoEntityController.getEntityAfterValidation(rid, user);
    List<MEntitySubEntityDistribution> resp = mEntitySubEntityDistRepo.getMEntitySubEntityDistributionsByEntityId(entity.getId());
    return ApiResp.<List<MEntitySubEntityDistribution>>builder().status(ApiResp.ResponseStatus.Success)
      .data(resp).build();
  }
}
