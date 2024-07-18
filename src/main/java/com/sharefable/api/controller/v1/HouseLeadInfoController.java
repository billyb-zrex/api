package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.Routes;
import com.sharefable.api.service.HouseLeadInfoService;
import com.sharefable.api.transport.resp.RespHouseLeadInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1)
@RequiredArgsConstructor
@Slf4j
public class HouseLeadInfoController {
  private final HouseLeadInfoService houseLeadInfoService;

  @RequestMapping(value = Routes.HOUSE_LEAD_INFO, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<RespHouseLeadInfo> getHouseLeadInfo(@RequestParam("org_id") Long orgId, @RequestParam("email") String email) {
    RespHouseLeadInfo houseLeadInfo = houseLeadInfoService.getHouseLeadInfo(orgId, email);
    return ApiResp.<RespHouseLeadInfo>builder().status(ApiResp.ResponseStatus.Success).data(houseLeadInfo).build();
  }
}
