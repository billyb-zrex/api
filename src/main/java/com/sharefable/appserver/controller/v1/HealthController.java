package com.sharefable.appserver.controller.v1;

import com.sharefable.appserver.common.ApiResp;
import com.sharefable.appserver.controller.Routes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(Routes.API_V1)
@Slf4j
public class HealthController {
    @RequestMapping(value = Routes.HEALTH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp health() {
        return ApiResp.builder()
            .status(ApiResp.ResponseStatus.Success)
            .data("ok")
            .build();
    }
}
