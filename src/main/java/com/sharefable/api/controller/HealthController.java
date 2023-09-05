package com.sharefable.api.controller;

import com.sharefable.api.common.ApiResp;
import com.sharefable.api.transport.resp.RespHealth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Slf4j
public class HealthController {
    @RequestMapping(value = "/unhandled", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String unhandledException() {
        log.info("Now we will throw 5xx");
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }

    @RequestMapping(value = "/handled", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
    public String handledException() {
        log.info("Now we will throw and handle exception internally");
        try {
            throw new RuntimeException("Forced exception");
        } catch (Exception e) {
            log.error("Caught excpetion {}", e.getMessage());
            e.printStackTrace();
        }
        return "ok";
    }

    @RequestMapping(value = Routes.HEALTH, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ApiResp<RespHealth> health() {
        return ApiResp.<RespHealth>builder()
            .status(ApiResp.ResponseStatus.Success)
            .data(RespHealth.builder().build())
            .build();
    }
}
