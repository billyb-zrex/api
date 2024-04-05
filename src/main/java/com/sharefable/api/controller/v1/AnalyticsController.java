package com.sharefable.api.controller.v1;

import com.sharefable.api.auth.AuthUser;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.User;
import com.sharefable.api.service.AnalyticsService;
import com.sharefable.api.transport.req.ReqAddOrUpdateLeadInfo;
import com.sharefable.api.transport.req.ReqLeadActivityDataPost;
import com.sharefable.api.transport.resp.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@RestController
@RequestMapping(Routes.API_V1)
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {
  private final AnalyticsService analyticsService;

  @RequestMapping(value = Routes.TOTAL_VIEWS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAuthority(@Perm.VIEW_ANALYTICS)")
  public ApiResp<RespTourView> getTotalViews(@RequestParam(name = "rid") String rid, @RequestParam(name = "d") Integer days, @AuthUser User user) {
    RespTourView resp = analyticsService.getTotalVisitorsForTour(rid, days, user);
    return ApiResp.<RespTourView>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.ANN_VIEWS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAuthority(@Perm.VIEW_ANALYTICS)")
  public ApiResp<RespTourAnnViews> getAnnViewInfo(@RequestParam(name = "rid") String rid, @RequestParam(name = "d") Integer days, @AuthUser User user) {
    RespTourAnnViews resp = analyticsService.getAnnViews(rid, days, user);
    return ApiResp.<RespTourAnnViews>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.STEPS_DURATION, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAuthority(@Perm.VIEW_ANALYTICS)")
  public ApiResp<RespTourAnnWithPercentile> getStepsVisited(@RequestParam(name = "rid") String rid, @RequestParam(name = "d") Integer days, @AuthUser User user) {
    RespTourAnnWithPercentile resp = analyticsService.getTimeSpentForEachAnnotation(rid, days, user);
    return ApiResp.<RespTourAnnWithPercentile>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.CONVERSION, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAuthority(@Perm.VIEW_ANALYTICS)")
  public ApiResp<RespConversion> getConversion(@RequestParam(name = "rid") String rid, @RequestParam(name = "d") Integer days, @AuthUser User user) {
    RespConversion resp = analyticsService.getTourConversion(rid, days, user);
    return ApiResp.<RespConversion>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.GET_ALL_TOUR_LEADS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAuthority(@Perm.VIEW_ANALYTICS)")
  public ApiResp<RespTourLeads> getLeadsForATour(@RequestParam(name = "rid") String rid, @RequestParam(name = "d") Integer days, @AuthUser User user) {
    RespTourLeads resp = analyticsService.getLeadsForATour(rid, days, user);
    return ApiResp.<RespTourLeads>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.UPLOAD_LEAD_LEVEL_ANALYTICS, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAuthority(@Perm.VIEW_ANALYTICS)")
  public ApiResp<RespLeadActivityUrl> uploadLeadActivity(@RequestBody ReqLeadActivityDataPost body) {
    RespLeadActivityUrl resp = analyticsService.uploadLeadActivityToS3(body);
    return ApiResp.<RespLeadActivityUrl>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.GET_LEAD_ACTIVITY_DATA_FILE, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @PreAuthorize("hasAuthority(@Perm.VIEW_ANALYTICS)")
  public ApiResp<RespLeadActivityUrl> getLeadActivityDataFile(@RequestParam(name = "rid") String rid, @RequestParam(name = "aid") String aid, @AuthUser User user) {
    RespLeadActivityUrl resp = analyticsService.getLeadActivityDataFile(rid, aid, user);
    return ApiResp.<RespLeadActivityUrl>builder().status(ApiResp.ResponseStatus.Success).data(resp).build();
  }

  @RequestMapping(value = Routes.LOG_USER_EVENTS_DIRECT, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<String> logEvents(@RequestParam("sub") String encodedSub, @RequestBody String userEventLogs) {
    String dSub = URLDecoder.decode(encodedSub, StandardCharsets.UTF_8);
    String sub = new String(Base64.getDecoder().decode(dSub));
    analyticsService.logUserEvents(sub, userEventLogs);
    return ApiResp.<String>builder().status(ApiResp.ResponseStatus.Success).data("ok").build();
  }

  @RequestMapping(value = Routes.ADD_OR_UPDATE_LEAD_INFO, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<String> addOrUpdateLeadInfo(@RequestBody ReqAddOrUpdateLeadInfo body) {
    analyticsService.updateLeadInfo(body);
    return ApiResp.<String>builder().status(ApiResp.ResponseStatus.Success).data("ok").build();
  }
}
