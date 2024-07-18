package com.sharefable.api.controller.v1;

import com.sharefable.Routes;
import com.sharefable.api.common.ApiResp;
import com.sharefable.api.service.EntityService;
import com.sharefable.api.transport.resp.RespCommonConfig;
import com.sharefable.api.transport.resp.RespDemoEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.javatuples.Pair;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
@RequiredArgsConstructor
public class DemoEntityController {
  private final EntityService entityService;
  private final WorkspaceController wsController;

  @RequestMapping(value = Routes.REPUBLISH_DATA_FILE_ONLY, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ApiResp<Pair<Boolean, RespDemoEntity>> getAllTours(@PathVariable("rid") String rid) {
    RespCommonConfig commonConfig = wsController.getCommonConfig().getData();
    Pair<Boolean, RespDemoEntity> resp = entityService.refreshAndPublishEntityDataFile(rid, commonConfig);
    return ApiResp.<Pair<Boolean, RespDemoEntity>>builder().status(ApiResp.ResponseStatus.Success)
      .data(resp).build();
  }
}
