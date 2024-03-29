package com.sharefable.api.controller.v1;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.controller.Routes;
import com.sharefable.api.service.Lead360Service;
import com.sharefable.api.transport.req.ReqHouseLeadInfoWithInfo360;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1)
@RequiredArgsConstructor
@Slf4j
public class Lead360Controller {
  private final Lead360Service lead360Service;

  @RequestMapping(value = Routes.POPULATED_LEAD_360, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<String> populatedLead360(@RequestBody ReqHouseLeadInfoWithInfo360 body) {
    lead360Service.saveLead360(body);
    return ApiResp.<String>builder().status(ApiResp.ResponseStatus.Success).data("ok").build();
  }
}
