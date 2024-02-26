package com.sharefable.api.controller.v1;

import com.sharefable.api.auth.AuthUser;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.entity.User;
import com.sharefable.api.service.AnalyticsService;
import com.sharefable.api.transport.resp.RespConversion;
import com.sharefable.api.transport.resp.RespTourAnnViews;
import com.sharefable.api.transport.resp.RespTourAnnWithPercentile;
import com.sharefable.api.transport.resp.RespTourView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1)
@RequiredArgsConstructor
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
}
