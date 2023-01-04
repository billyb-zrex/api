package com.sharefable.api.controller;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.transport.RespHealth;
import com.sharefable.api.transport.ResponseBase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class HealthController {
    @RequestMapping(value = Routes.HEALTH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<ResponseBase> health() {
        return ApiResp.builder()
            .status(ApiResp.ResponseStatus.Success)
            .data(RespHealth.builder().build())
            .build();
    }
}
